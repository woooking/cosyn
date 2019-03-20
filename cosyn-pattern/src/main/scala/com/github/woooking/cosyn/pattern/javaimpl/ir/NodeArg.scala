package com.github.woooking.cosyn.pattern.javaimpl.ir

import com.github.woooking.cosyn.pattern.javaimpl.cfg.{CFG, CFGStatements}

trait NodeArg {

}

object NodeArg {
    object NoArg extends NodeArg

    abstract class ArgCFG(val cfg: CFG) extends NodeArg {
        val block: CFGStatements
    }
}

