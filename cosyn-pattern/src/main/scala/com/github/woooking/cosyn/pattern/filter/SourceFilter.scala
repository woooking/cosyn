package com.github.woooking.cosyn.pattern.filter

trait SourceFilter[Data] {
    def valid(file: Data): Boolean
}
