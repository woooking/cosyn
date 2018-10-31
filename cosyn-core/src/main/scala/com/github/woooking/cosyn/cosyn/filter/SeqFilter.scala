package com.github.woooking.cosyn.cosyn.filter

trait SeqFilter[T] {
    def filter(data: Seq[T]): Seq[T]
}

object SeqFilter {
    def apply[T](filter: SingleFilter[T]): SeqFilter[T] = (data: Seq[T]) => data.filter(filter.valid)

    def apply[T](func: T => Boolean): SeqFilter[T] = (data: Seq[T]) => data.filter(func)
}