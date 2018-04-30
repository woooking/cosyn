package com.github.woooking.cosyn.cosyn.filter

case class CombinedFilter[T](seqFilters: Seq[SeqFilter[T]]) extends SeqFilter[T] {
    override def filter(data: Seq[T]): Seq[T] = (data /: seqFilters) ((d, fs) => fs filter d)
}
