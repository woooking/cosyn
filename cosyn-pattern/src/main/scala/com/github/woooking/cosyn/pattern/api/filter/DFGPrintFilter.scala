package com.github.woooking.cosyn.pattern.api.filter

import cats.syntax.show._
import com.github.woooking.cosyn.pattern.javaimpl.dfg.SimpleDFG
import org.slf4s.Logging

/**
  * 一个DFGPrintFilter是对一个<code>Seq[SimpleDFG]</code>过滤的Filter
  */
case object DFGPrintFilter extends SeqFilter[SimpleDFG] with Logging {
    def valid(dfg: SimpleDFG): Boolean = {
        log.info(dfg.show)
        true
    }
}
