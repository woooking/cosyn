package com.github.woooking.cosyn.dfgprocessor

import com.github.javaparser.ast.`type`.Type
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.typesystem.ReferenceTypeImpl
import com.github.woooking.cosyn.dfgprocessor.cfg.{CFGImpl, CFGStatements}
import com.github.woooking.cosyn.dfgprocessor.ir._
import com.github.woooking.cosyn.javaparser.expr._

class QualifiedVisitor(javaParserFacade: JavaParserFacade) extends SimpleVisitor {
    override def resolveParameterType(p: Parameter): String = {
        try {
            javaParserFacade.getType(p).describe()
        } catch {
            case _: Throwable => super.resolveParameterType(p)
        }
    }

    override def resolveType(ty: Type): String = try {
        javaParserFacade.convertToUsage(ty).describe()
    } catch {
        case _: Throwable => super.resolveType(ty)
    }

    override def resolveMethodCallExpr(methodCallExpr: MethodCallExpr): String = try {
        val result = javaParserFacade.solve(methodCallExpr.delegate)
        if (result.isSolved) result.getCorrespondingDeclaration.getQualifiedSignature
        else super.resolveMethodCallExpr(methodCallExpr)
    } catch {
        case _: Throwable => super.resolveMethodCallExpr(methodCallExpr)
    }

    override def resolveObjectCreationExpr(objectCreationExpr: ObjectCreationExpr): String = try {
        val result = javaParserFacade.solve(objectCreationExpr.delegate)
        if (result.isSolved) result.getCorrespondingDeclaration.getQualifiedSignature
        else super.resolveObjectCreationExpr(objectCreationExpr)
    } catch {
        case _: Throwable => super.resolveObjectCreationExpr(objectCreationExpr)
    }

    override def visitFieldAccess(cfg: CFGImpl)(block: CFGStatements, node: FieldAccessExpr): IRExpression = {
        resolveEnumField(node) match {
            case Some(enum) => enum
            case None => super.visitFieldAccess(cfg)(block, node)
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
