package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{BlockStmt, HoleFactory}

object FillCellColor {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            call(
                holeFactory.newHole(),
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
                        holeFactory.newHole()
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
            holeFactory.newHole(),
            "org.apache.poi.ss.usermodel.Cell",
            "setCellStyle",
            arg(
                "setCellStyle(org.apache.poi.ss.usermodel.CellStyle)",
                "style",
            )
        )
    )
    val pattern = Pattern(holeFactory, stmt)
}
