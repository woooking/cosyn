package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.expr._
import com.github.woooking.cosyn.code.model.expr.NameExpr._
import com.github.woooking.cosyn.code.model.stmt.BlockStmt
import com.github.woooking.cosyn.code.model.stmt.ExprStmt._
import com.github.woooking.cosyn.code.model.ty.{ArrayType, BasicType}

object CreateConditionalFormatting {
    val holes = Seq.fill(7)(HoleExpr())
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
                        "byte",
                        holes(1)
                    ),
                ),
                MethodCallArgs(
                    "java.lang.String",
                    holes(2)
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
            MethodCallArgs("boolean", holes(3)),
            MethodCallArgs("boolean", holes(4)),
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
                        holes(5)
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
            MethodCallArgs(ArrayType(BasicType("org.apache.poi.ss.util.CellRangeAddress")), holes(6)),
            MethodCallArgs("org.apache.poi.ss.usermodel.ConditionalFormattingRule", "rule"),
        ),
    )
    val pattern = new Pattern(stmt, holes)
}
