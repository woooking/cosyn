package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.expr._
import com.github.woooking.cosyn.code.model.stmt.ExprStmt._
import com.github.woooking.cosyn.code.model.stmt.BlockStmt

object CreateHyperlink {
    val holes = Seq.fill(4)(HoleExpr())
    val stmt = BlockStmt.of(
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.CreationHelper",
            "creationHelper",
            MethodCallExpr(
                holes(0),
                "org.apache.poi.ss.usermodel.Workbook",
                "getCreationHelper",
            )
        ),
        VariableDeclaration(
            "org.apache.poi.ss.usermodel.Hyperlink",
            "link",
            MethodCallExpr(
                NameExpr("creationHelper"),
                "org.apache.poi.ss.usermodel.CreationHelper",
                "createHyperlink",
                MethodCallArgs(
                    "org.apache.poi.common.usermodel.HyperlinkType",
                    EnumConstantExpr(
                        "org.apache.poi.common.usermodel.HyperlinkType",
                        holes(1)
                    ),
                )
            )
        ),
        MethodCallExpr(
            NameExpr("link"),
            "org.apache.poi.common.usermodel.Hyperlink",
            "setAddress",
            MethodCallArgs(
                "java.lang.String",
                holes(2)
            )
        ),
        MethodCallExpr(
            holes(3),
            "org.apache.poi.ss.usermodel.Cell",
            "setHyperlink",
            MethodCallArgs(
                "org.apache.poi.ss.usermodel.Hyperlink",
                NameExpr("link")
            )
        ),
    )
    val pattern = new Pattern(stmt, holes)
}
