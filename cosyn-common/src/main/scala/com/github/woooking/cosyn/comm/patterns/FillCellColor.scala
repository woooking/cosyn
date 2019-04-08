package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, BlockStmt, HoleFactory}

object FillCellColor {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
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
            "setFillForegroundColor",
            arg(
                "short",
                call(
                    enum(
                        BasicType("org.apache.poi.ss.usermodel.IndexedColors"),
                        holeFactory.newHole()
                    ),
                    BasicType("org.apache.poi.ss.usermodel.IndexedColors"),
                    "getIndex",
                )
            ),
        ),
        call(
            "style",
            BasicType("org.apache.poi.ss.usermodel.CellStyle"),
            "setFillPattern",
            arg(
                "org.apache.poi.ss.usermodel.FillPatternType",
                enum(
                    BasicType("org.apache.poi.ss.usermodel.FillPatternType"),
                    "SOLID_FOREGROUND",
                ),
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
