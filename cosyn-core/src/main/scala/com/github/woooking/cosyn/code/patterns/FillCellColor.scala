package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.expr._
import com.github.woooking.cosyn.code.model.stmt.ExprStmt._
import com.github.woooking.cosyn.code.model.expr.NameExpr._
import com.github.woooking.cosyn.code.model.expr.MethodCallExpr
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

object FillCellColor {
    val holes = Seq.fill(3)(HoleExpr())
    val stmt = BlockStmt.of(
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            MethodCallExpr(
                holes(0),
                "org.apache.poi.ss.usermodel.Workbook",
                "createCellStyle",
            )
        ),
        MethodCallExpr(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle",
            "setFillForegroundColor",
            MethodCallArgs(
                "short",
                MethodCallExpr(
                    EnumConstantExpr(
                        "org.apache.poi.ss.usermodel.IndexedColors",
                        holes(1)
                    ),
                    "org.apache.poi.ss.usermodel.IndexedColors",
                    "getIndex",
                )
            ),
        ),
        MethodCallExpr(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle",
            "setFillPattern",
            MethodCallArgs(
                "org.apache.poi.ss.usermodel.FillPatternType",
                EnumConstantExpr(
                    "org.apache.poi.ss.usermodel.FillPatternType",
                    "SOLID_FOREGROUND",
                ),
            )
        ),
        MethodCallExpr(
            holes(2),
            "org.apache.poi.ss.usermodel.Cell",
            "setCellStyle",
            MethodCallArgs(
                "setCellStyle(org.apache.poi.ss.usermodel.CellStyle)",
                "style",
            )
        )
    )
    val pattern = new Pattern(stmt, holes)
}
