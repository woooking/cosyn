package com.github.woooking.cosyn.kg.repository.impl

import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, Type}
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.kg.entity.{EnumEntity, PatternEntity, TypeEntity}
import com.github.woooking.cosyn.kg.repository.TypeEntityRepository

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.immutable.Queue
import scala.collection.mutable

class TypeEntityRepositoryImpl extends TypeEntityRepository {
    def getAllPatterns: List[PatternEntity] = {
        session.loadAll(classOf[PatternEntity]).asScala.toList
    }

    private def getIterablePaths(entity: TypeEntity, path: List[TypeEntity]): Set[List[TypeEntity]] = {
        val typeEntity = get(entity.getQualifiedName)
        val newPath = typeEntity :: path
        (Set(newPath) /: typeEntity.getIterables.asScala) ((s, t) => s ++ getIterablePaths(t, newPath))
    }

    def getIterablePaths(basicType: BasicType): Set[List[TypeEntity]] = profile("getIterablePaths") {
        getIterablePaths(get(basicType.ty), Nil)
    }

    def get(qualifiedName: String): TypeEntity = profile("getTypeEntity") {
        session.load(classOf[TypeEntity], qualifiedName)
    }

    private def getAllNonAbstractSubTypesRec(typeEntity: TypeEntity): Set[TypeEntity] = {
        val entity = get(typeEntity.getQualifiedName)
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
            val source = get(h.getQualifiedName)
            if (source == target) true
            else isAssignableRec(t ++ source.getExtendedTypes.asScala, target, visited + source)
    }

    @tailrec
    private def isAssignableRec(source: Type, target: Type): Boolean = {
        (source, target) match {
            case (ArrayType(s), ArrayType(t)) => isAssignableRec(s, t)
            case (BasicType(s), BasicType(t)) =>
                val sourceEntity = get(s)
                val targetEntity = get(t)
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
        val typeEntity = session.load(classOf[TypeEntity], receiverType.ty)
        typeEntity.getStaticFields.getFieldsInfo.getOrDefault(targetType.toString, java.util.List.of()).asScala.toSet
    }
}
