package com.github.woooking.cosyn.knowledge_graph

import com.github.javaparser.ast.AccessSpecifier
import com.github.woooking.cosyn.entity.{EnumEntity, MethodEntity, TypeEntity}
import com.github.woooking.cosyn.code.Context
import com.github.woooking.cosyn.code.model.ty.{ArrayType, BasicType, Type}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{Session, SessionFactory}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri("bolt://162.105.88.181")
        .credentials("neo4j", "poi")
        .build()

    val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.entity")

    val session: Session = sessionFactory.openSession()

    private def getIterablePaths(qualifiedName: String, path: List[TypeEntity]): Set[List[TypeEntity]] = {
        val entity = getTypeEntity(qualifiedName)
        val newPath = entity :: path
        (Set(newPath) /: entity.getIterables.asScala) ((s, t) => s ++ getIterablePaths(t.getQualifiedName, newPath))
    }

    def getIterablePaths(basicType: BasicType): Set[List[TypeEntity]] = {
        getIterablePaths(basicType.ty, Nil)
    }

    def getMethodJavadoc(qualifiedSignature: String): Option[String] = {
        val methodEntity = session.load(classOf[MethodEntity], qualifiedSignature)
        methodEntity.getJavadoc match {
            case javadoc if javadoc == "" =>
                methodEntity.getExtendedMethods.asScala.toStream
                    .map(_.getQualifiedSignature)
                    .map(getMethodJavadoc)
                    .filter(_.isDefined)
                    .map(_.get)
                    .headOption
            case javadoc => Some(javadoc)
        }
    }

    def getMethodEntity(qualifiedSignature: String): MethodEntity = {
        session.load(classOf[MethodEntity], qualifiedSignature)
    }

    def getTypeEntity(qualifiedName: String): TypeEntity = {
        session.load(classOf[TypeEntity], qualifiedName)
    }

    def getAllNonAbstractSubTypes(typeEntity: TypeEntity): Set[TypeEntity] = {
        val entity = session.load(classOf[TypeEntity], typeEntity.getQualifiedName)
        val subTypes = entity.getSubTypes.asScala.toSet
        val initSet: Set[TypeEntity] = if (entity.isAbstract || entity.isInterface) Set() else Set(entity)
        (initSet /: subTypes) ((ts, t) => ts ++ getAllNonAbstractSubTypes(t))
    }

    private def isAssignable(source: TypeEntity, target: TypeEntity): Boolean = {
        val sourceEntity = session.load(classOf[TypeEntity], source.getQualifiedName)
        val targetEntity = session.load(classOf[TypeEntity], target.getQualifiedName)
        if (sourceEntity == targetEntity) true
        else sourceEntity.getExtendedTypes.asScala.exists(e => isAssignable(e, targetEntity))
    }

    @tailrec
    def isAssignable(source: Type, target: Type): Boolean = {
        (source, target) match {
            case (ArrayType(s), ArrayType(t)) => isAssignable(s, t)
            case (BasicType(s), BasicType(t)) =>
                val sourceEntity = session.load(classOf[TypeEntity], s)
                val targetEntity = session.load(classOf[TypeEntity], t)
                if (sourceEntity == null || targetEntity == null) return false
                isAssignable(sourceEntity, targetEntity)
            case _ =>
                false
        }

    }

    private def isAccessible(context: Context, entity: MethodEntity): Boolean = {
        val methodEntity = session.load(classOf[MethodEntity], entity.getQualifiedSignature)
        // TODO: 考虑继承和protected
        methodEntity.getAccessSpecifier == AccessSpecifier.PUBLIC ||
            methodEntity.getDeclareType.isInterface && methodEntity.getAccessSpecifier == AccessSpecifier.DEFAULT
    }

    private def producers(context: Context, typeEntity: TypeEntity, multiple: Boolean): Set[MethodEntity] = {
        val entity = session.load(classOf[TypeEntity], typeEntity.getQualifiedName)
        val methods = (if (multiple) entity.getMultipleProducers else entity.getProducers).asScala.filter(isAccessible(context, _))
        (methods.toSet /: entity.getSubTypes.asScala) ((methods, subType) => methods ++ producers(context, subType, multiple))
    }

    def producers(context: Context, ty: Type): Set[MethodEntity] = {
        ty match {
            case BasicType(t) =>
                val typeEntity = session.load(classOf[TypeEntity], t)
                producers(context, typeEntity, multiple = false)
                    .map(m => session.load(classOf[MethodEntity], m.getQualifiedSignature))
                    .filter(!_.isDeprecated)
            case ArrayType(BasicType(t)) =>
                val typeEntity = session.load(classOf[TypeEntity], t)
                producers(context, typeEntity, multiple = true)
                    .map(m => session.load(classOf[MethodEntity], m.getQualifiedSignature))
                    .filter(!_.isDeprecated)
            case _ => ???
        }

    }

    def enumConstants(ty: BasicType): Set[String] = {
        val typeEntity = session.load(classOf[EnumEntity], ty.ty)
        typeEntity.getConstants.split(",").toSet
    }

    def staticFields(receiverType: BasicType, targetType: Type): Set[String] = {
        val typeEntity = session.load(classOf[TypeEntity], receiverType.ty)
        typeEntity.getStaticFields.getFieldsInfo.getOrDefault(targetType.toString, java.util.List.of()).asScala.toSet
    }

    def close(): Unit = {
        sessionFactory.close()
    }
}
