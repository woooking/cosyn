package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.CodeBuilder._
import com.github.woooking.cosyn.code.model.ty.ArrayType
import com.github.woooking.cosyn.code.model.{BlockStmt, BooleanLiteral, HoleExpr}

object CreateDropDownList {
    """
      |HSSFWorkbook workbook = new HSSFWorkbook();
      |  HSSFSheet sheet = workbook.createSheet("Data Validation");
      |  CellRangeAddressList addressList = new CellRangeAddressList(
      |    0, 0, 0, 0);
      |  DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(
      |    new String[]{"10", "20", "30"});
      |  DataValidation dataValidation = new HSSFDataValidation
      |    (addressList, dvConstraint);
      |  dataValidation.setSuppressDropDownArrow(false);
      |  sheet.addValidationData(dataValidation);
    """.stripMargin
    val holes: Seq[HoleExpr] = Seq.fill(3)(HoleExpr())
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.util.CellRangeAddressList",
            "address",
            create(
                "org.apache.poi.ss.util.CellRangeAddressList",
                arg("int", holes.head),
                arg("int", holes(1)),
                arg("int", holes(2)),
                arg("int", holes(3)),
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
                    holes(4)
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
            holes(5),
            "org.apache.poi.ss.usermodel.Sheet",
            "addValidationData",
            arg("org.apache.poi.ss.usermodel.DataValidation", "dataValidation")
        )
    )
    val pattern = new Pattern(stmt, holes)
}
