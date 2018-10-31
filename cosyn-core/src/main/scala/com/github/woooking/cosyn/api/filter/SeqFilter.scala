package com.github.woooking.cosyn.api.filter

import com.github.woooking.cosyn.api.Pipeline.Filter

object SeqFilter {
    def fromCondition[T](condition: T => Boolean): Filter[Seq[T]] = (input: Seq[T]) => input.filter(condition)
}
