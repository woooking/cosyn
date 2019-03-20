package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.kg.repository.{MethodEntityRepository, PatternRepository, TypeEntityRepository}
import com.github.woooking.cosyn.kg.repository.impl.{MethodEntityRepositoryImpl, PatternRepositoryImpl, TypeEntityRepositoryImpl}

object Components {
    import com.softwaremill.macwire._

    lazy val patternRepository: PatternRepository = wire[PatternRepositoryImpl]
    lazy val typeEntityRepository: TypeEntityRepository = wire[TypeEntityRepositoryImpl]
    lazy val methodEntityRepository: MethodEntityRepository = wire[MethodEntityRepositoryImpl]
}
