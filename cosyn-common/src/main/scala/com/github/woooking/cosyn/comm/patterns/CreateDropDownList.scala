package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model._

object CreateDropDownList {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.util.CellRangeAddressList",
            "address",
            create(
                BasicType("org.apache.poi.ss.util.CellRangeAddressList"),
                arg("int", holeFactory.newHole()),
                arg("int", holeFactory.newHole()),
                arg("int", holeFactory.newHole()),
                arg("int", holeFactory.newHole()),
            ),
        ),
        v(
            "org.apache.poi.ss.usermodel.DataValidationConstraint",
            "dvConstraint",
            call(
                "DVConstraint",
                BasicType("org.apache.poi.hssf.usermodel.DVConstraint"),
                "createExplicitListConstraint",
                arg(
                    ArrayType("java.lang.string"),
                    holeFactory.newHole()
                )
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.DataValidation",
            "dataValidation",
            create(
                BasicType("org.apache.poi.hssf.usermodel.HSSFDataValidation"),
                arg("org.apache.poi.ss.util.CellRangeAddressList", "address"),
                arg("org.apache.poi.ss.usermodel.DataValidationConstraint", "dvConstraint"),
            )
        ),
        call(
            "dataValidation",
            BasicType("org.apache.poi.ss.usermodel.DataValidation"),
            "setSuppressDropDownArrow",
            arg("boolean", BooleanLiteral(false))
        ),
        call(
            holeFactory.newHole(),
            BasicType("org.apache.poi.ss.usermodel.Sheet"),
            "addValidationData",
            arg("org.apache.poi.ss.usermodel.DataValidation", "dataValidation")
        )
    )
    val pattern = Pattern(holeFactory, stmt)
}
