package com.github.woooking.cosyn.code.patterns

import com.github.woooking.cosyn.code.Pattern
import com.github.woooking.cosyn.code.model.CodeBuilder._
import com.github.woooking.cosyn.code.model.{BlockStmt, HoleExpr}

object CreateHyperlink {
    val holes: Seq[HoleExpr] = Seq.fill(4)(HoleExpr())
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CreationHelper",
            "creationHelper",
            call(
                holes.head,
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
                        holes(1)
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
                holes(2)
            )
        ),
        call(
            holes(3),
            "org.apache.poi.ss.usermodel.Cell",
            "setHyperlink",
            arg(
                "org.apache.poi.ss.usermodel.Hyperlink",
                "link"
            )
        ),
    )
    val pattern = new Pattern(stmt, holes)
}
