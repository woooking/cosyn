package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.CodeBuilder._
import com.github.woooking.cosyn.code.model.{BlockStmt, HoleExpr}

object FillCellColor {
    val holes: Seq[HoleExpr] = Seq.fill(3)(HoleExpr())
    val stmt: BlockStmt = block(
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
            "setFillForegroundColor",
            arg(
                "short",
                call(
                    enum(
                        "org.apache.poi.ss.usermodel.IndexedColors",
                        holes(1)
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
