package com.github.woooking.cosyn.util

import java.io.PrintStream

trait Printable[T] {
    def print(obj: T, ps: PrintStream = System.out): Unit
}

