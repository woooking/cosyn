package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, BlockStmt, HoleFactory}

object TestPattern {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            call(
                "sheet",
                BasicType("org.apache.poi.ss.usermodel.Workbook"),
                "createCellStyle",
            )
        ),
    )
    val pattern = Pattern(holeFactory, stmt)
}
