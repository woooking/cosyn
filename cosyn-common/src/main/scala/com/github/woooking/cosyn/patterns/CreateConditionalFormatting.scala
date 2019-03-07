package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.skeleton.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{ArrayType, BasicType, BlockStmt, HoleFactory}

object CreateConditionalFormatting {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "sheetCF",
            call(
                holeFactory.newHole(),
                "org.apache.poi.ss.usermodel.Sheet",
                "getSheetConditionalFormatting",
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.ConditionalFormattingRule",
            "rule",
            call(
                "sheetCF",
                "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
                "createConditionalFormattingRule",
                arg(
                    "byte",
                    field(
                        "org.apache.poi.ss.usermodel.ComparisonOperator",
                        "byte",
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
                "org.apache.poi.ss.usermodel.ConditionalFormattingRule",
                "createFontFormatting",
            )
        ),
        call(
            "fontFmt",
            "org.apache.poi.ss.usermodel.FontFormatting",
            "setFontStyle",
            arg("boolean", holeFactory.newHole()),
            arg("boolean", holeFactory.newHole()),
        ),
        call(
            "fontFmt",
            "org.apache.poi.ss.usermodel.FontFormatting",
            "setFontColorIndex",
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
            "sheetCF",
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "addConditionalFormatting",
            arg(ArrayType(BasicType("org.apache.poi.ss.util.CellRangeAddress")), holeFactory.newHole()),
            arg("org.apache.poi.ss.usermodel.ConditionalFormattingRule", "rule"),
        ),
    )
    val pattern = Pattern(holeFactory, stmt)
}
