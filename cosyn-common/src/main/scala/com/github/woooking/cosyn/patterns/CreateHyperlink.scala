package com.github.woooking.cosyn.patterns

import com.github.woooking.cosyn.Pattern
import com.github.woooking.cosyn.skeleton.model.CodeBuilder._
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, HoleFactory}

object CreateHyperlink {
    val holeFactory = new HoleFactory()
    val stmt: BlockStmt = block(
        v(
            "org.apache.poi.ss.usermodel.CreationHelper",
            "creationHelper",
            call(
                holeFactory.newHole(),
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
                        holeFactory.newHole()
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
                holeFactory.newHole()
            )
        ),
        call(
            holeFactory.newHole(),
            "org.apache.poi.ss.usermodel.Cell",
            "setHyperlink",
            arg(
                "org.apache.poi.ss.usermodel.Hyperlink",
                "link"
            )
        ),
    )
    val pattern = Pattern(holeFactory, stmt)
}
