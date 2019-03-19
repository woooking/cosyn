package com.github.woooking.cosyn.kg.repository

import com.github.woooking.cosyn.kg.Repository
import com.github.woooking.cosyn.kg.entity.PatternEntity

trait PatternRepository extends Repository {
    def getAllPatterns: List[PatternEntity]
}
