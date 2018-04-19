package com.github.woooking.cosyn.cosyn.filter

trait SingleFilter[T] {
    def valid(data: T): Boolean
}
