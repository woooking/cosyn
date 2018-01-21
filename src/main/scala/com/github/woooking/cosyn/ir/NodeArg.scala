package com.github.woooking.cosyn.ir

import com.github.woooking.cosyn.cfg.CFG

trait NodeArg {

}

object NodeArg {
    object NoArg extends NodeArg

    abstract class ArgCFG(val cfg: CFG) extends NodeArg {
        val block: cfg.Statements
    }
}

