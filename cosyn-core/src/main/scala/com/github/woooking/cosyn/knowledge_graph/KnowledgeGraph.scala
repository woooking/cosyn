package com.github.woooking.cosyn.knowledge_graph

import com.github.javaparser.ast.AccessSpecifier
import com.github.woooking.cosyn.entity.{EnumEntity, MethodEntity, TypeEntity}
import com.github.woooking.cosyn.pattern.Context
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{Session, SessionFactory}

import scala.collection.JavaConverters._

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri("bolt://162.105.88.181")
        .credentials("neo4j", "poi")
        .build()

    val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.entity")

    val session: Session = sessionFactory.openSession()

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

    def isAssignable(source: String, target: String): Boolean = {
        val sourceEntity = session.load(classOf[TypeEntity], source)
        val targetEntity = session.load(classOf[TypeEntity], target)
        if (sourceEntity == null || targetEntity == null) return false
        isAssignable(sourceEntity, targetEntity)
    }

    private def isAccessible(context: Context, methodEntity: MethodEntity): Boolean = {
        // TODO: 考虑继承和protected
        methodEntity.getAccessSpecifier == AccessSpecifier.PUBLIC
    }

    private def producers(context: Context, typeEntity: TypeEntity): Set[MethodEntity] = {
        val entity = session.load(classOf[TypeEntity], typeEntity.getQualifiedName)
        val methods = entity.getProducers.asScala.filter(isAccessible(context, _))
        (methods.toSet /: entity.getSubTypes.asScala) ((methods, subType) => methods ++ producers(context, subType))
    }

    def producers(context: Context, ty: String): Set[MethodEntity] = {
        val typeEntity = session.load(classOf[TypeEntity], ty)
        producers(context, typeEntity).map(m => session.load(classOf[MethodEntity], m.getQualifiedSignature))
    }

    def enumConstants(ty: String): Set[String] = {
        val typeEntity = session.load(classOf[EnumEntity], ty)
        typeEntity.getConstants.split(",").toSet
    }

    def close(): Unit = {
        sessionFactory.close()
    }
}
