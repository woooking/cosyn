package com.github.woooking.cosyn.comm.patterns

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.comm.skeleton.model.{BasicType, BlockStmt, HoleFactory}

object CreateHyperlink {
    val holeFactory = HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CreationHelper",
            "creationHelper",
            call(
                holeFactory.newHole(),
                BasicType("org.apache.poi.ss.usermodel.Workbook"),
                "getCreationHelper",
            )
        ),
        v(
            "org.apache.poi.ss.usermodel.Hyperlink",
            "link",
            call(
                "creationHelper",
                BasicType("org.apache.poi.ss.usermodel.CreationHelper"),
                "createHyperlink",
                arg(
                    "org.apache.poi.common.usermodel.HyperlinkType",
                    enum(
                        BasicType("org.apache.poi.common.usermodel.HyperlinkType"),
                        holeFactory.newHole()
                    ),
                )
            )
        ),
        call(
            "link",
            BasicType("org.apache.poi.common.usermodel.Hyperlink"),
            "setAddress",
            arg(
                "java.lang.String",
                holeFactory.newHole()
            )
        ),
        call(
            holeFactory.newHole(),
            BasicType("org.apache.poi.ss.usermodel.Cell"),
            "setHyperlink",
            arg(
                "org.apache.poi.ss.usermodel.Hyperlink",
                "link"
            )
        ),
    )
    val pattern = Pattern(holeFactory, stmt)
}