package com.github.woooking.cosyn.pattern.api

import de.parsemis.graph.{Graph => ParsemisGraph}

/**
  * 从挖掘出的图数据生成代码模式的抽象接口
  * @tparam N 结点类型
  * @tparam E 边类型
  * @tparam Graph 图类型
  * @tparam R 代码模式类型
  */
trait PatternGenerator[N, E, Graph, R] {
    def generate(originalGraph: Seq[Graph])(graph: ParsemisGraph[N, E]): R
}
