package com.github.woooking.cosyn

import com.github.woooking.cosyn.cfg.CFG

package object ir {
    trait NodeArg

    object NoArg extends NodeArg

    case class Block(block: CFG#Statements) extends NodeArg

    trait NodeResult

    object NoResult extends NodeResult

    case class ListResult(results: Seq[NodeResult]) extends NodeResult
}
