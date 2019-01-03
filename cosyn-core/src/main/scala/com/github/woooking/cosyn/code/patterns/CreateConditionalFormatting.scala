package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.expr._
import com.github.woooking.cosyn.code.model.ty.{ArrayType, BasicType}
import com.github.woooking.cosyn.code.model.CodeBuilder._
import com.github.woooking.cosyn.code.model.HoleExpr

object CreateConditionalFormatting {
    val holes = Seq.fill(7)(HoleExpr())
    val stmt = block(
        v(
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "sheetCF",
            call(
                holes(0),
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
                        holes(1)
                    ),
                ),
                arg(
                    "java.lang.String",
                    holes(2)
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
            arg("boolean", holes(3)),
            arg("boolean", holes(4)),
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
                        holes(5)
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
            MethodCallArgs(ArrayType(BasicType("org.apache.poi.ss.util.CellRangeAddress")), holes(6)),
            MethodCallArgs("org.apache.poi.ss.usermodel.ConditionalFormattingRule", "rule"),
        ),
    )
    val pattern = new Pattern(stmt, holes)
}
