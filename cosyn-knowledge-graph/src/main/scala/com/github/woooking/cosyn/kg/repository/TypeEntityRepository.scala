package com.github.woooking.cosyn.kg.repository

import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, Type}
import com.github.woooking.cosyn.kg.Repository
import com.github.woooking.cosyn.kg.entity.TypeEntity

trait TypeEntityRepository extends Repository {

    def getIterablePaths(basicType: BasicType): Set[List[TypeEntity]]

    def getAllNonAbstractSubTypes(typeEntity: TypeEntity): Set[TypeEntity]

    def isAssignable(source: Type, target: Type): Boolean

    def enumConstants(ty: BasicType): Set[String]

    def staticFields(receiverType: BasicType, targetType: Type): Set[String]
}
