package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.pattern.model.expr.NameExpr._
import com.github.woooking.cosyn.pattern.model.expr.{MethodCallExpr, _}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern.model.stmt.ExprStmt._

object FillCellColor {
    val p = BlockStmt(
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.CellStyle",
            "style",
            MethodCallExpr(
                HoleExpr,
                "org.apache.poi.ss.usermodel.Workbook.createCellStyle()",
            )
        ),
        MethodCallExpr(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle.setFillForegroundColor(short)",
            MethodCallExpr(
                EnumConstantExpr(
                    "org.apache.poi.ss.usermodel.IndexedColors",
                    HoleExpr
                ),
                "org.apache.poi.ss.usermodel.IndexedColors.getIndex()"
            )
        ),
        MethodCallExpr(
            "style",
            "org.apache.poi.ss.usermodel.CellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType)",
            EnumConstantExpr(
                "org.apache.poi.ss.usermodel.FillPatternType",
                HoleExpr
            ),
        ),
        MethodCallExpr(
            HoleExpr,
            "org.apache.poi.ss.usermodel.Cell.setCellStyle(org.apache.poi.ss.usermodel.CellStyle)",
            "style",
        )
    )
}
