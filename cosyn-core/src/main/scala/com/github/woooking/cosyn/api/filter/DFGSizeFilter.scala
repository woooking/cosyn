package com.github.woooking.cosyn.api.filter

import com.github.woooking.cosyn.dfgprocessor.dfg.SimpleDFG

case class DFGSizeFilter(maxSize: Int) extends SeqFilter[SimpleDFG] {
    def valid(dfg: SimpleDFG): Boolean = dfg.getNodeCount <= maxSize
}
