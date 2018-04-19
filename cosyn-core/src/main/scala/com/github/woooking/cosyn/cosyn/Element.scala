package com.github.woooking.cosyn.cosyn

import scala.collection.mutable.ArrayBuffer

trait Element[In, Out] {
    val outs: ArrayBuffer[Element[Out, _]] = collection.mutable.ArrayBuffer()

    def connect[T](outElement: Element[Out, T]): outElement.type = {
        outs += outElement
        outElement
    }

    def process(input: In): Out

    def start(input: In): Unit = {
        val output = process(input)
        outs.foreach(_.start(output))
    }
}
