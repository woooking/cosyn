package com.github.woooking.cosyn.cosyn

class Collector[T] extends Element[T, Unit] {
    var result: Option[T] = None

    override def process(input: T): Unit = {
        result = Some(input)
    }

    def get: T = result match {
        case None => throw new RuntimeException("Collector has not started yet.")
        case Some(v) => v
    }
}
