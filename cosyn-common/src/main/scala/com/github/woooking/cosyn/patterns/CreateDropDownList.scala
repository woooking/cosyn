package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.ArrayType
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, BooleanLiteral, HoleExpr}

object CreateDropDownList {
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.util.CellRangeAddressList",
            "address",
            create(
                "org.apache.poi.ss.util.CellRangeAddressList",
                arg("int", HoleExpr()),
                arg("int", HoleExpr()),
                arg("int", HoleExpr()),
                arg("int", HoleExpr()),
            ),
        ),
        v(
            "org.apache.poi.ss.usermodel.DataValidationConstraint",
            "dvConstraint",
            call(
                "DVConstraint",
                "org.apache.poi.hssf.usermodel.DVConstraint",
                "createExplicitListConstraint",
                arg(
                    ArrayType("java.lang.string"),
                    HoleExpr()
                )
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.DataValidation",
            "dataValidation",
            create(
                "org.apache.poi.hssf.usermodel.HSSFDataValidation",
                arg("org.apache.poi.ss.util.CellRangeAddressList", "address"),
                arg("org.apache.poi.ss.usermodel.DataValidationConstraint", "dvConstraint"),
            )
        ),
        call(
            "dataValidation",
            "org.apache.poi.ss.usermodel.DataValidation",
            "setSuppressDropDownArrow",
            arg("boolean", BooleanLiteral(false))
        ),
        call(
            HoleExpr(),
            "org.apache.poi.ss.usermodel.Sheet",
            "addValidationData",
            arg("org.apache.poi.ss.usermodel.DataValidation", "dataValidation")
        )
    )
    val pattern = Pattern(stmt)
}
