package com.github.woooking.cosyn.pattern.patterns

import com.github.woooking.cosyn.pattern.Pattern
import com.github.woooking.cosyn.pattern.model.expr._
import com.github.woooking.cosyn.pattern.model.expr.NameExpr._
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern.model.stmt.ExprStmt._

object CreateConditionalFormatting {
    val holes = Seq.fill(6)(HoleExpr())
    val stmt = BlockStmt.of(
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "sheetCF",
            MethodCallExpr(
                holes(0),
                "org.apache.poi.ss.usermodel.Sheet",
                "getSheetConditionalFormatting",
            )
        ),
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.ConditionalFormattingRule",
            "rule",
            MethodCallExpr(
                "sheetCF",
                "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
                "createConditionalFormattingRule",
                MethodCallArgs(
                    "byte",
                    StaticFieldAccessExpr(
                        "org.apache.poi.ss.usermodel.ComparisonOperator",
                        holes(1)
                    ),
                )
            )
        ),
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.FontFormatting",
            "fontFmt",
            MethodCallExpr(
                "rule",
                "org.apache.poi.ss.usermodel.ConditionalFormattingRule",
                "createFontFormatting",
            )
        ),
        MethodCallExpr(
            "fontFmt",
            "org.apache.poi.ss.usermodel.FontFormatting",
            "setFontStyle",
            MethodCallArgs("boolean", holes(2)),
            MethodCallArgs("boolean", holes(3)),
        ),
        MethodCallExpr(
            "fontFmt",
            "org.apache.poi.ss.usermodel.FontFormatting",
            "setFontColorIndex",
            MethodCallArgs(
                "short",
                MethodCallExpr(
                    EnumConstantExpr(
                        "org.apache.poi.ss.usermodel.IndexedColors",
                        holes(4)
                    ),
                    "org.apache.poi.ss.usermodel.IndexedColors",
                    "getIndex",
                )
            ),
        ),
        MethodCallExpr(
            "sheetCF",
            "org.apache.poi.ss.usermodel.SheetConditionalFormatting",
            "addConditionalFormatting",
            MethodCallArgs("org.apache.poi.ss.util.CellRangeAddress[]", holes(5)),
            MethodCallArgs("org.apache.poi.ss.usermodel.ConditionalFormattingRule", "rule"),
        ),
    )
    val pattern = new Pattern(stmt, holes)
}
