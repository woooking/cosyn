package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, HoleFactory}

object ChangeFontFamily {
    val holeFactory = new HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.Font",
            "font",
            call(
                holeFactory.newHole(),
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
                holeFactory.newHole()
            )
        ),
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
            "setFont",
            arg(
                "org.apache.poi.ss.usermodel.Font",
                "font"
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
