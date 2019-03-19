package com.github.woooking.cosyn.kg.repository

import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, Type}
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.kg.Repository
import com.github.woooking.cosyn.kg.entity.{MethodEntity, TypeEntity}

trait MethodEntityRepository extends Repository {
    def get(qualifiedSignature: String): MethodEntity

    def producers(ty: Type): Set[MethodEntity]

}
