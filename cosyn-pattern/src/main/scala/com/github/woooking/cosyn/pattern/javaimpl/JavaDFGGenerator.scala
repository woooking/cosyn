package com.github.woooking.cosyn.pattern.javaimpl

import better.files.File
import com.github.woooking.cosyn.pattern.CosynConfig
import com.github.woooking.cosyn.pattern.api.GraphGenerator
import com.github.woooking.cosyn.pattern.api.filter.{CuImportFilter, DFGNodeFilter, DFGPrintFilter}
import com.github.woooking.cosyn.pattern.javaimpl.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.pattern.javaimpl.dfg.{DFGNode, SimpleDFG}
import com.github.woooking.cosyn.pattern.util.GraphUtil

case class JavaDFGGenerator() extends GraphGenerator[File, SimpleDFG] {
    override def generate(data: File): Seq[SimpleDFG] = {
        val projectParser = new JavaProjectParser()
//            .register(CuImportFilter("import org.apache.poi"))
            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, CosynConfig.global.methodFullQualifiedSignature)))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "org.apache.poi.ss.usermodel.CellStyle.setFillForegroundColor(short)")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "org.apache.poi.ss.usermodel.CreationHelper.createHyperlink(org.apache.poi.common.usermodel.HyperlinkType)")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "org.apache.poi.hssf.usermodel.DVConstraint.createExplicitListConstraint(java.lang.String[])")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "org.apache.poi.ss.usermodel.Font.setFontName(java.lang.String)")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "org.apache.poi.ss.usermodel.Cell.setCellComment(org.apache.poi.ss.usermodel.Comment)")))
        if (CosynConfig.global.debug) {
            projectParser.register(DFGPrintFilter)
        }
        val dfgs = data.path >>: projectParser
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "createHyperlink")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "parse")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "render")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "render")))
//            .register(DFGNodeFilter(DFGNode(NodeType.MethodInvocation, "IndexWriter::init")))
//            .register(DFGSizeFilter(50))
        dfgs
    }
}
