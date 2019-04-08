package com.github.woooking.cosyn.core

import com.github.woooking.cosyn.kg.repository.impl.{MethodEntityRepositoryImpl, PatternRepositoryImpl, TypeEntityRepositoryImpl}
import com.github.woooking.cosyn.kg.repository.{MethodEntityRepository, PatternRepository, TypeEntityRepository}

object Components {
    import com.softwaremill.macwire._

    lazy val patternRepository: PatternRepository = wire[PatternRepositoryImpl]
    lazy val typeEntityRepository: TypeEntityRepository = wire[TypeEntityRepositoryImpl]
    lazy val methodEntityRepository: MethodEntityRepository = wire[MethodEntityRepositoryImpl]
}
