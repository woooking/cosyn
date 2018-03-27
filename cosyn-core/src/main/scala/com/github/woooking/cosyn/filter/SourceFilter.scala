package com.github.woooking.cosyn.filter

trait SourceFilter[Data] {
    def valid(file: Data): Boolean
}
