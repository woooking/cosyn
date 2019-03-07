package com.github.woooking.cosyn.pattern.api.filter

import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.SimpleDFG

/**
  * 一个DFGSizeFilter是对一个<code>Seq[SimpleDFG]</code>过滤的Filter，只保留结点数不超过<code>maxSize</code>的[[SimpleDFG]]
  * @param maxSize 保留的[[SimpleDFG]]包含结点数量上限
  */
case class DFGSizeFilter(maxSize: Int) extends SeqFilter[SimpleDFG] {
    def valid(dfg: SimpleDFG): Boolean = dfg.getNodeCount <= maxSize
}
