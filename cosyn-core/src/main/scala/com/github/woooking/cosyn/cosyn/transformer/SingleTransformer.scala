package com.github.woooking.cosyn.cosyn.transformer

import com.github.woooking.cosyn.cosyn.Element

case class SingleTransformer[T, U](func: T => U) extends Element[T, U] {
    override def process(input: T): U = func(input)
}
