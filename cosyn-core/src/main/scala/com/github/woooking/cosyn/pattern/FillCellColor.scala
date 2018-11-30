package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.pattern.model.expr.NameExpr._
import com.github.woooking.cosyn.pattern.model.expr.{MethodCallExpr, _}
import com.github.woooking.cosyn.pattern.model.stmt.BlockStmt
import com.github.woooking.cosyn.pattern.model.stmt.ExprStmt._

object FillCellColor {
    val holes = Seq.fill(3)(HoleExpr())
    val stmt = BlockStmt(
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
