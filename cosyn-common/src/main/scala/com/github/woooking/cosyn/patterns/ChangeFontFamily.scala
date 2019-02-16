package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, HoleExpr}

object ChangeFontFamily {
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.Font",
            "font",
            call(
                HoleExpr(),
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
                HoleExpr()
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            call(
                HoleExpr(),
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
            HoleExpr(),
            "org.apache.poi.ss.usermodel.Cell",
            "setCellStyle",
            arg(
                "setCellStyle(org.apache.poi.ss.usermodel.CellStyle)",
                "style",
            )
        )
    )
    val pattern = Pattern(stmt)
}
