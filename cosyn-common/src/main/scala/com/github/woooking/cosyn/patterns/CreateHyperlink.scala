package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, HoleExpr}

object CreateHyperlink {
    val holes: Seq[HoleExpr] = Seq.fill(4)(HoleExpr())
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CreationHelper",
            "creationHelper",
            call(
                HoleExpr(),
                "org.apache.poi.ss.usermodel.Workbook",
                "getCreationHelper",
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.Hyperlink",
            "link",
            call(
                "creationHelper",
                "org.apache.poi.ss.usermodel.CreationHelper",
                "createHyperlink",
                arg(
                    "org.apache.poi.common.usermodel.HyperlinkType",
                    enum(
                        "org.apache.poi.common.usermodel.HyperlinkType",
                        HoleExpr()
                    ),
                )
            )
        ),
        call(
            "link",
            "org.apache.poi.common.usermodel.Hyperlink",
            "setAddress",
            arg(
                "java.lang.String",
                HoleExpr()
            )
        ),
        call(
            HoleExpr(),
            "org.apache.poi.ss.usermodel.Cell",
            "setHyperlink",
            arg(
                "org.apache.poi.ss.usermodel.Hyperlink",
                "link"
            )
        ),
    )
    val pattern = Pattern(stmt)
}
