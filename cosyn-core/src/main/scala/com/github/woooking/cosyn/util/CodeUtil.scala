package com.github.woooking.cosyn.util

import com.github.woooking.cosyn.javaparser.CompilationUnit

object CodeUtil {
    def packageOf(cu: CompilationUnit): String = {
        cu.packageDeclaration match {
            case None => ""
            case Some(packageDeclaration) => packageDeclaration.name
        }
    }
}
