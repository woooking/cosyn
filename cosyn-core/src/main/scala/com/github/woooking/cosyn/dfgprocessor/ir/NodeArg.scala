package com.github.woooking.cosyn.dfgprocessor.ir

import com.github.woooking.cosyn.dfgprocessor.cfg.{CFGImpl, CFGStatements}

trait NodeArg {

}

object NodeArg {
    object NoArg extends NodeArg

    abstract class ArgCFG(val cfg: CFGImpl) extends NodeArg {
        val block: CFGStatements
    }
}

