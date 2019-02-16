package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.{ArrayType, BasicType}
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.HoleExpr

object CreateConditionalFormatting {
    val stmt = block(
        v(
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "sheetCF",
            call(
                HoleExpr(),
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
                        HoleExpr()
                    ),
                ),
                arg(
                    "java.lang.String",
                    HoleExpr()
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
            arg("boolean", HoleExpr()),
            arg("boolean", HoleExpr()),
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
                        HoleExpr()
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
            arg(ArrayType(BasicType("org.apache.poi.ss.util.CellRangeAddress")), HoleExpr()),
            arg("org.apache.poi.ss.usermodel.ConditionalFormattingRule", "rule"),
        ),
    )
    val pattern = Pattern(stmt)
}
