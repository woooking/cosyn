package com.github.woooking.cosyn.pattern.filter

trait SingleFilter[T] {
    def valid(data: T): Boolean
}
