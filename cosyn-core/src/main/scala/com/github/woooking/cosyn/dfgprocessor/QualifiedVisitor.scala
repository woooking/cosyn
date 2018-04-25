package com.github.woooking.cosyn.dfgprocessor

import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl
import com.github.woooking.cosyn.dfgprocessor.cfg.{CFGImpl, CFGStatements}
import com.github.woooking.cosyn.dfgprocessor.ir._
import com.github.woooking.cosyn.dfgprocessor.ir.statements._
import com.github.woooking.cosyn.javaparser.expr._

class QualifiedVisitor(javaParserFacade: JavaParserFacade) extends SimpleVisitor {

    override def visitMethodCallExpr(cfg: CFGImpl)(block: CFGStatements, node: MethodCallExpr): IRExpression = {
        val MethodCallExpr(_, scope, _, arguments) = node
        val (name, qualified) = qualifiedMethodCallExpr(node)
        val receiver = scope.map(node => visitExpression(cfg)(block, node))
        val args = arguments.map(node => visitExpression(cfg)(block, node))
        block.addStatement(IRMethodInvocation(cfg, name, qualified, receiver, args, Set(node))).target
    }

    override def visitFieldAccess(cfg: CFGImpl)(block: CFGStatements, node: FieldAccessExpr): IRExpression = {
        resolveEnumField(node) match {
            case Some(enum) => enum
            case None => super.visitFieldAccess(cfg)(block, node)
        }
    }

    private def qualifiedMethodCallExpr(methodCall: MethodCallExpr): (String, Boolean) = {
        try {
            val result = javaParserFacade.solve(methodCall.delegate)
            if (result.isSolved) (result.getCorrespondingDeclaration.getQualifiedSignature, true)
            else (methodCall.name, false)
        } catch {
            case _: Throwable => (methodCall.name, false)
        }
    }

    private def resolveEnumField(fieldAccess: FieldAccessExpr): Option[IREnum] = {
        try {
            val result = javaParserFacade.solve(fieldAccess.delegate)
            if (result.isSolved) {
                val resolvedType = result.getCorrespondingDeclaration.getType
                if (resolvedType.isReferenceType) {
                    val receiverType = resolvedType.asInstanceOf[ReferenceTypeImpl].getTypeDeclaration
                    if (receiverType.isEnum) {
                        return Some(IREnum(receiverType.asEnum().getQualifiedName, fieldAccess.name))
                    }
                }
            }
            None
        } catch {
            case _: Throwable => None
        }
    }

}
