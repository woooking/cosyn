package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, BlockStmt, HoleFactory}

object CreateConditionalFormatting {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "sheetCF",
            call(
                holeFactory.newHole(),
                BasicType("org.apache.poi.ss.usermodel.Sheet"),
                "getSheetConditionalFormatting",
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.ConditionalFormattingRule",
            "rule",
            call(
                "sheetCF",
                BasicType("org.apache.poi.ss.usermodel.SheetConditionalFormatting"),
                "createConditionalFormattingRule",
                arg(
                    "byte",
                    field(
                        BasicType("org.apache.poi.ss.usermodel.ComparisonOperator"),
                        BasicType("byte"),
                        holeFactory.newHole()
                    ),
                ),
                arg(
                    "java.lang.String",
                    holeFactory.newHole()
                )
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.FontFormatting",
            "fontFmt",
            call(
                "rule",
                BasicType("org.apache.poi.ss.usermodel.ConditionalFormattingRule"),
                "createFontFormatting",
            )
        ),
        call(
            "fontFmt",
            BasicType("org.apache.poi.ss.usermodel.FontFormatting"),
            "setFontStyle",
            arg("boolean", holeFactory.newHole()),
            arg("boolean", holeFactory.newHole()),
        ),
        call(
            "fontFmt",
            BasicType("org.apache.poi.ss.usermodel.FontFormatting"),
            "setFontColorIndex",
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
            "sheetCF",
            BasicType("org.apache.poi.ss.usermodel.SheetConditionalFormatting"),
            "addConditionalFormatting",
            arg("org.apache.poi.ss.util.CellRangeAddress[]", holeFactory.newHole()),
            arg("org.apache.poi.ss.usermodel.ConditionalFormattingRule", "rule"),
        ),
    )
    val pattern = Pattern(holeFactory, stmt)
}
