package com.github.woooking.cosyn.pattern.javaimpl

import better.files.File
import com.github.woooking.cosyn.pattern.CosynConfig
import com.github.woooking.cosyn.pattern.api.GraphGenerator
import com.github.woooking.cosyn.pattern.api.filter.DFGNodeFilter
import com.github.woooking.cosyn.pattern.javaimpl.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.pattern.javaimpl.dfg.{DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.util.GraphUtil

case class JavaDFGGenerator() extends GraphGenerator[File, SimpleDFG] {
    override def generate(data: File): Seq[SimpleDFG] = {
        val dfgs = data.path >>: new JavaProjectParser()
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
