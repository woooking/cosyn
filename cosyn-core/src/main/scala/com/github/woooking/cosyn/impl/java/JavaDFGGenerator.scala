package com.github.woooking.cosyn.impl.java

import java.nio.file.Path

import com.github.woooking.cosyn.CosynConfig
import com.github.woooking.cosyn.api.GraphGenerator
import com.github.woooking.cosyn.api.filter.{DFGNodeFilter, DFGSizeFilter}
import com.github.woooking.cosyn.dfgprocessor.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGNode, SimpleDFG}
import com.github.woooking.cosyn.util.GraphUtil

case class JavaDFGGenerator() extends GraphGenerator[Path, SimpleDFG] {
    override def generate(data: Path): Seq[SimpleDFG] = {
        val dfgs = data >>: new JavaProjectParser()
            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "org.apache.poi.ss.usermodel.CellStyle.setFillForegroundColor(short)")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "createHyperlink")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "parse")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "render")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "IndexWriter::init")))
//            .register(DFGSizeFilter(50))
        if (CosynConfig.global.debug) {
            dfgs.foreach(d => {
                println("---------")
                println(d.cfg.decl)
                println()
                GraphUtil.printGraph()(d)
            })
        }
        dfgs
    }
}
