package com.github.woooking.cosyn.util

import java.io.PrintStream

trait Printable {
    def print(ps: PrintStream = System.out): Unit
}
