package com.github.woooking.cosyn.cfg

import java.io.PrintStream

class Switch(cfg: CFG) extends CFGBlock(cfg) {
    override def setNext(next: CFGBlock): Unit = throw new Exception("Cannot set next block of switch")

    override def print(ps: PrintStream = System.out): Unit = ???
}