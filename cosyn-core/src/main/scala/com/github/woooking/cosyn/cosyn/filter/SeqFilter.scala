package com.github.woooking.cosyn.cosyn.filter

import com.github.woooking.cosyn.cosyn.Element

trait SeqFilter[T] extends Element[Seq[T], Seq[T]] {
    def valid(data: Seq[T]): Seq[T]

    override def process(input: Seq[T]): Seq[T] = valid(input)
}

object SeqFilter {
    def apply[T](filter: SingleFilter[T]): SeqFilter[T] = (data: Seq[T]) => data.filter(filter.valid)

    def apply[T](func: T => Boolean): SeqFilter[T] = (data: Seq[T]) => data.filter(func)
}