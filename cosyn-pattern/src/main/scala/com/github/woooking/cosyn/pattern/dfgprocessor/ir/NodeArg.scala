package com.github.woooking.cosyn.pattern.dfgprocessor.ir

import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.{CFGImpl, CFGStatements}

trait NodeArg {

}

object NodeArg {
    object NoArg extends NodeArg

    abstract class ArgCFG(val cfg: CFGImpl) extends NodeArg {
        val block: CFGStatements
    }
}

