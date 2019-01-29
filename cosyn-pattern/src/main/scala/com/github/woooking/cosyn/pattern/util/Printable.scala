package com.github.woooking.cosyn.pattern.util

import java.io.PrintStream

trait Printable[T] {
    def print(obj: T, ps: PrintStream): Unit
}

object Printable {
    def print[T: Printable](obj: T, ps: PrintStream = System.out): Unit = {
        val printable = implicitly[Printable[T]]
        printable.print(obj, ps)
    }
}