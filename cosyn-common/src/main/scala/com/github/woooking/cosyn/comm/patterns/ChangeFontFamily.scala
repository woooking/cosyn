package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, BlockStmt, HoleFactory}

object ChangeFontFamily {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.Font",
            "font",
            call(
                holeFactory.newHole(),
                BasicType("org.apache.poi.ss.usermodel.Workbook"),
                "createFont",
            )
        ),
        call(
            "font",
            BasicType("org.apache.poi.ss.usermodel.Font"),
            "setFontName",
            arg(
                "java.lang.String",
                holeFactory.newHole()
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            call(
                holeFactory.newHole(),
                BasicType("org.apache.poi.ss.usermodel.Workbook"),
                "createCellStyle",
            )
        ),
        call(
            "style",
            BasicType("org.apache.poi.ss.usermodel.CellStyle"),
            "setFont",
            arg(
                "org.apache.poi.ss.usermodel.Font",
                "font"
            )
        ),
        call(
            holeFactory.newHole(),
            BasicType("org.apache.poi.ss.usermodel.Cell"),
            "setCellStyle",
            arg(
                "setCellStyle(org.apache.poi.ss.usermodel.CellStyle)",
                "style",
            )
        )
    )
    val pattern = Pattern(holeFactory, stmt)
}
