package com.github.woooking.cosyn.middleware

import com.github.javaparser.ast.expr.{MethodCallExpr, ObjectCreationExpr}
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import com.github.woooking.cosyn.javaparser.CompilationUnit

class ObjectCreationCUFilter(name: String) extends CompilationUnitFilter {
    class Visitor extends GenericVisitorAdapter[java.lang.Boolean, Boolean] {
        override def visit(n: ObjectCreationExpr, arg: Boolean): java.lang.Boolean = {
            if (n.getType.asString() == name) true
            else super.visit(n, arg)
        }
    }

    override def valid(file: CompilationUnit): Boolean = {
        val result = file.delegate.accept(new Visitor(), false)
        if (result) true else false
    }

}
