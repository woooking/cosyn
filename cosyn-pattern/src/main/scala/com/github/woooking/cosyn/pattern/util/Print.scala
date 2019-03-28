package com.github.woooking.cosyn.pattern.util

import java.io.PrintStream

import cats.Show
import cats.implicits._

object Print {
    def print[T: Show](obj: T, ps: PrintStream = System.out): Unit = {
        ps.println(obj.show)
    }
}