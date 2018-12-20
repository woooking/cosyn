package com.github.woooking.cosyn.pattern.dfgprocessor.ir

trait NodeResult

object NodeResult {
    object NoResult extends NodeResult

    case class ListResult(results: Seq[NodeResult]) extends NodeResult
}