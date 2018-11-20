package com.github.woooking.cosyn.filter
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.visitor.GenericVisitorAdapter

class MethodCallCUFilter(name: String) extends NodeFilter {
    class Visitor extends GenericVisitorAdapter[java.lang.Boolean, Boolean] {
        override def visit(n: MethodCallExpr, arg: Boolean): java.lang.Boolean = {
            if (n.getName.asString() == name) true
            else super.visit(n, arg)
        }
    }

    override def valid(node: Node): Boolean = {
        val result = node.accept(new Visitor(), false)
        if (result) true else false
    }

}
