package com.github.woooking.cosyn.kg.repository

import com.github.woooking.cosyn.comm.skeleton.model.Type
import com.github.woooking.cosyn.kg.Repository
import com.github.woooking.cosyn.kg.entity.MethodEntity

trait MethodEntityRepository extends Repository {
    def get(qualifiedSignature: String): MethodEntity

    def producers(ty: Type): Set[MethodEntity]

    def getMethodProto(method: String): String
}
