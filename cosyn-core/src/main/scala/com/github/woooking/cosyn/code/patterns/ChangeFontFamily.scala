package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.CodeBuilder._
import com.github.woooking.cosyn.code.model.{BlockStmt, HoleExpr}

object ChangeFontFamily {
    val holes: Seq[HoleExpr] = Seq.fill(3)(HoleExpr())
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.Font",
            "font",
            call(
                holes.head,
                "org.apache.poi.ss.usermodel.Workbook",
                "createFont",
            )
        ),
        call(
            "font",
            "org.apache.poi.ss.usermodel.Font",
            "setFontName",
            arg(
                "java.lang.String",
                holes(1)
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            call(
                holes.head,
                "org.apache.poi.ss.usermodel.Workbook",
                "createCellStyle",
            )
        ),
        call(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle",
            "setFont",
            arg(
                "org.apache.poi.ss.usermodel.Font",
                "font"
            )
        ),
        call(
            holes(2),
            "org.apache.poi.ss.usermodel.Cell",
            "setCellStyle",
            arg(
                "setCellStyle(org.apache.poi.ss.usermodel.CellStyle)",
                "style",
            )
        )
    )
    val pattern = new Pattern(stmt, holes)
}
