package com.github.woooking.cosyn.pattern.api.filter

import com.github.woooking.cosyn.pattern.api.Pipeline.Filter

trait SeqFilter[T] extends Filter[Seq[T]] {
    def valid(input: T): Boolean

    override final def >>:(input: Seq[T]): Seq[T] = input filter valid
}
