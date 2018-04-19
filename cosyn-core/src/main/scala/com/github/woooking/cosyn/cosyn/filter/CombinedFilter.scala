package com.github.woooking.cosyn.cosyn.filter

case class CombinedFilter[T](seqFilters: Seq[SeqFilter[T]]) extends SeqFilter[T] {
    override def valid(data: Seq[T]): Seq[T] = (data /: seqFilters) ((d, fs) => fs valid d)
}
