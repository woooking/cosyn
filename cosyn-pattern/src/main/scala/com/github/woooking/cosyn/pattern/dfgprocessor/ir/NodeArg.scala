package com.github.woooking.cosyn.pattern.dfgprocessor.ir

import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.{CFG, CFGStatements}

trait NodeArg {

}

object NodeArg {
    object NoArg extends NodeArg

    abstract class ArgCFG(val cfg: CFG) extends NodeArg {
        val block: CFGStatements
    }
}

