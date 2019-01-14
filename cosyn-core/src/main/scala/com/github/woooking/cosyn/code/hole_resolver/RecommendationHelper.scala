package com.github.woooking.cosyn.code.hole_resolver

import com.github.woooking.cosyn.code._
import com.github.woooking.cosyn.code.model.CodeBuilder._
import com.github.woooking.cosyn.code.model.{ArrayType, BasicType, HoleExpr, Type}
import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.knowledge_graph.KnowledgeGraph
import com.github.woooking.cosyn.util.CodeUtil

object RecommendationHelper {
    def recommendationForType(context: Context, ty: Type): List[Pattern] = {
        ty match {
            case bt @ BasicType(t) =>
                val vars = context.findVariables(bt)
                val producers = KnowledgeGraph.producers(context, bt)
                val simpleName = CodeUtil.qualifiedClassName2Simple(t).toLowerCase
                val q = s"Which $simpleName?"
                ???
            case at @ ArrayType(BasicType(t)) =>
                ???
            case _ =>
                ???
        }

    }

    private def choose(context: Context, pattern: Pattern, hole: HoleExpr, method: MethodEntity): (Context, Pattern) = method match {
        case _ if method.isConstructor =>
            ???
        case _ if method.isStatic =>
            val receiverType = method.getDeclareType.getQualifiedName
            val args = CodeUtil.methodParams(method.getSignature).map(ty => arg(ty, HoleExpr()))
            val newPattern = pattern.fillHole(hole, call(CodeUtil.qualifiedClassName2Simple(receiverType), receiverType, method.getSimpleName, args: _*))
            (context, newPattern)
        case _ =>
            val receiverType = method.getDeclareType.getQualifiedName
            val receiver = HoleExpr()
            val args = CodeUtil.methodParams(method.getSignature).map(ty => arg(ty, HoleExpr()))
            val newPattern = pattern.fillHole(hole, call(receiver, receiverType, method.getSimpleName, args: _*))
            (context, newPattern)
    }
}
