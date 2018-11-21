package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.pattern.model.expr.HoleExpr
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt

class Pattern(val stmts: BlockStmt, holes: Seq[HoleExpr]) {

}

object Pattern {
    val patterns = Map(
        "fill cell color" -> FillCellColor.pattern
    )

    def main(args: Array[String]): Unit = {
//        val testFile = Resource.my.getAsStream("/Test1.java")
//        val cu = JavaParser.parse(testFile)
        val resolver = new ReceiverHoleSolver
        val context = new Context
        context.variables += "wb" -> "org.apache.poi.hssf.usermodel.HSSFWorkbook"
        println(resolver.resolve(FillCellColor.pattern.stmts, FillCellColor.holes(0), context))
    }
}
