package com.github.woooking.cosyn.cosyn.transformer

import com.github.woooking.cosyn.cosyn.Element

case class SeqMapTransformer[T, U](func: T => U) extends Element[Seq[T], Seq[U]] {
    override def process(input: Seq[T]): Seq[U] = input.map(func)
}
