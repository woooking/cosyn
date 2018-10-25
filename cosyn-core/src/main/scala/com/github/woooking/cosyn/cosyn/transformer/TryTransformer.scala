package com.github.woooking.cosyn.cosyn.transformer

import com.github.woooking.cosyn.cosyn.Element
import org.slf4s.Logging

case class TryTransformer[T, U](func: T => U) extends Element[Seq[T], Seq[U]] {
    override def process(input: Seq[T]): Seq[U] = input.flatMap(i => try {
        Seq(func(i))
    } catch {
        case e: Throwable => e.printStackTrace()
            Seq.empty
    })
}
