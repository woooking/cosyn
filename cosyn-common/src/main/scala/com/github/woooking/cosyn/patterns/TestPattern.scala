package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, HoleExpr}

object TestPattern {
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            call(
                "sheet",
                "org.apache.poi.ss.usermodel.Workbook",
                "createCellStyle",
            )
        ),
        call(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle",
            "setFillForegroundColor",
            arg(
                "short",
                call(
                    enum(
                        "org.apache.poi.ss.usermodel.IndexedColors",
                        "RED"
                    ),
                    "org.apache.poi.ss.usermodel.IndexedColors",
                    "getIndex",
                )
            ),
        ),
        call(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle",
            "setFillPattern",
            arg(
                "org.apache.poi.ss.usermodel.FillPatternType",
                enum(
                    "org.apache.poi.ss.usermodel.FillPatternType",
                    "SOLID_FOREGROUND",
                ),
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
