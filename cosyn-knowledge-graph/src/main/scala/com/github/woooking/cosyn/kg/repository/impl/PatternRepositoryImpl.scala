package com.github.woooking.cosyn.kg.repository.impl

import com.github.woooking.cosyn.kg.entity.PatternEntity
import com.github.woooking.cosyn.kg.repository.PatternRepository
import scala.collection.JavaConverters._

class PatternRepositoryImpl extends PatternRepository {
    def getAllPatterns: List[PatternEntity] = {
        session.loadAll(classOf[PatternEntity]).asScala.toList
    }
}
