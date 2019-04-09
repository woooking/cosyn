package com.github.woooking.cosyn.kg.repository.impl

import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, Type}
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.kg.entity.{EnumEntity, TypeEntity}
import com.github.woooking.cosyn.kg.repository.TypeEntityRepository

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.immutable.Queue

class TypeEntityRepositoryImpl extends TypeEntityRepository {
    private def getIterablePaths(entity: TypeEntity, path: List[TypeEntity]): Set[List[TypeEntity]] = {
//        val typeEntity = getType(entity.getQualifiedName)
        val typeEntity = entity
        val newPath = typeEntity :: path
        (Set(newPath) /: typeEntity.getIterables.asScala) ((s, t) => s ++ getIterablePaths(t, newPath))
    }

    def getIterablePaths(basicType: BasicType): Set[List[TypeEntity]] = profile("getIterablePaths") {
        getIterablePaths(getType(basicType.ty), Nil)
    }

    private def getAllNonAbstractSubTypesRec(typeEntity: TypeEntity): Set[TypeEntity] = {
//        val entity = getType(typeEntity.getQualifiedName)
        val entity = typeEntity
        val subTypes = entity.getSubTypes.asScala.toSet
        val initSet: Set[TypeEntity] = if (entity.isAbstract || entity.isInterface) Set() else Set(entity)
        (initSet /: subTypes) ((ts, t) => ts ++ getAllNonAbstractSubTypesRec(t))
    }

    def getAllNonAbstractSubTypes(typeEntity: TypeEntity): Set[TypeEntity] = profile("getAllNonAbstractSubTypes") {
        getAllNonAbstractSubTypesRec(typeEntity)
    }

    @tailrec
    private def isAssignableRec(queue: Queue[TypeEntity], target: TypeEntity, visited: Set[TypeEntity]): Boolean = queue.dequeueOption match {
        case None => false
        case Some((h, t)) if visited.contains(h) =>
            isAssignableRec(t, target, visited)
        case Some((h, t)) =>
//            val source = getType(h.getQualifiedName)
            val source = h
            if (source == target) true
            else isAssignableRec(t ++ source.getExtendedTypes.asScala, target, visited + source)
    }

    @tailrec
    private def isAssignableRec(source: Type, target: Type): Boolean = {
        (source, target) match {
            case (ArrayType(s), ArrayType(t)) => isAssignableRec(s, t)
            case (BasicType(s), BasicType(t)) =>
                val sourceEntity = getType(s)
                val targetEntity = getType(t)
                if (sourceEntity == null || targetEntity == null) return false
                isAssignableRec(Queue(sourceEntity), targetEntity, Set.empty)
            case _ =>
                false
        }
    }

    def isAssignable(source: Type, target: Type): Boolean = profile("isAssignable") {
        isAssignableRec(source, target)
    }

    def enumConstants(ty: BasicType): Set[String] = profile("enumConstants") {
        val typeEntity = session.load(classOf[EnumEntity], ty.ty)
        typeEntity.getConstants.split(",").toSet
    }

    def staticFields(receiverType: BasicType, targetType: Type): Set[String] = profile("staticFields") {
        val typeEntity = getType(receiverType.ty)
        typeEntity.getStaticFields.getFieldsInfo.getOrDefault(targetType.toString, java.util.List.of()).asScala.toSet
    }

    private def getAllParentTypes(typeEntity: TypeEntity): Set[String] =
        (Set(typeEntity.getQualifiedName) /: typeEntity.getExtendedTypes.asScala) ((s, t) => s ++ getAllParentTypes(t))

    override def getAllParentTypes(qualifiedName: String): Set[String] = profile("getAllParentTypes") {
        getType(qualifiedName) match {
            case null => Set(qualifiedName)
            case typeEntity => getAllParentTypes(typeEntity)
        }
    }
}
