package com.github.woooking.cosyn.api.impl.java

import java.nio.file.Path

import com.github.woooking.cosyn.api.GraphGenerator
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.filter.{DFGFilter, NodeFilter}

import scala.collection.mutable

case class JavaDFGGenerator(maxNode: Option[Int]) extends GraphGenerator[Path, SimpleDFG] {
    private[this] val nodeFilters = mutable.ArrayBuffer[NodeFilter]()
    private[this] val dfgFilters = mutable.ArrayBuffer[DFGFilter]()

    def register(filter: NodeFilter): Unit = {
        nodeFilters += filter
    }

    def register(filter: DFGFilter): Unit = {
        dfgFilters += filter
    }

    private def pipeline(cfgs: Seq[CFGImpl]): Seq[SimpleDFG] = {
        cfgs.map(SimpleDFG.apply)
    }

    override def generate(data: Path): Seq[SimpleDFG] = {
        val cfgs = JavaProjectParser.parse(data)
        val dfgs = pipeline(cfgs)
        maxNode match {
            case None => dfgs
            case Some(n) => dfgs.filter(_.getNodeCount < n)
        }
    }
}
