package com.github.woooking.cosyn

import com.github.woooking.cosyn.cfg.CFG

package object ir {
    case class NodeArg(block: CFG#Statements)

    trait NodeResult

    object NoResult extends NodeResult
}
