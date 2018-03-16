package com.github.woooking.cosyn.filter
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.GenericVisitorAdapter
import com.github.woooking.cosyn.javaparser.CompilationUnit

class MethodCallCUFilter(name: String) extends CompilationUnitFilter {
    class Visitor extends GenericVisitorAdapter[java.lang.Boolean, Boolean] {
        override def visit(n: MethodCallExpr, arg: Boolean): java.lang.Boolean = {
            if (n.getName.asString() == name) true
            else super.visit(n, arg)
        }
    }

    override def valid(file: CompilationUnit): Boolean = {
        val result = file.delegate.accept(new Visitor(), false)
        if (result) true else false
    }

}
