package com.github.woooking.cosyn.core.code.hole_resolver

import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, Type}
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.core.Components
import com.github.woooking.cosyn.core.code._
import com.github.woooking.cosyn.core.code.rules.{CreateMethodJudger, GetMethodJudger, LoadMethodJudger}
import com.github.woooking.cosyn.core.config.Config
import com.github.woooking.cosyn.kg.entity.{EnumEntity, MethodEntity}

object QAHelper {

    sealed abstract class MethodCategory(val questionGenerator: String => String)

    object MethodCategory {

        final case object Create extends MethodCategory(ty => s"Create a new ${CodeUtil.qualifiedClassName2Simple(ty)}")

        final case object Load extends MethodCategory(ty => s"Load a ${CodeUtil.qualifiedClassName2Simple(ty)}")

        final case object Get extends MethodCategory(ty => s"Get a ${CodeUtil.qualifiedClassName2Simple(ty)}")

    }

    private final case object OtherType extends MethodCategory(_ => "Unknown")

    private val methodEntityRepository = Components.methodEntityRepository

    def choiceQAForType(context: Context, ty: Type, recommend: Boolean): ChoiceQuestion = profile("choice-for-type") {
        ty match {
            case bt @ BasicType(t) =>
                val vars = context.findVariables(bt)
                val producers = methodEntityRepository.producers(bt)

                if (recommend) {
                    // t是枚举类型，则添加枚举选项
                    val enumChoice = methodEntityRepository.getType(t) match {
                        case e: EnumEntity =>
                            EnumChoice(e) :: Nil
                        case _ =>
                            Nil
                    }

                    val simpleName = CodeUtil.qualifiedClassName2Simple(t).toLowerCase
                    val q = s"Which $simpleName?"
                    ChoiceQuestion(q, vars.toSeq.map(VariableChoice.apply) ++ enumChoice ++ producers.map(MethodChoice.apply))
                } else {
                    val cases: Map[MethodCategory, Set[MethodEntity]] = producers.groupBy {
                        case m if CreateMethodJudger.judge(m) => MethodCategory.Create
                        case m if LoadMethodJudger.judge(m) => MethodCategory.Load
                        case m if GetMethodJudger.judge(m) => MethodCategory.Get
                        case _ =>
                            OtherType
                    }
                    val methodCategoryChoices = cases.flatMap {
                        case (OtherType, m) =>
                            if (Config.printUnCategorisedMethods) {
                                println("----- UnCategorised -----")
                                m.foreach(f => {
                                    println(f.getQualifiedSignature)
                                    println(Option(f.getJavadoc).getOrElse(""))
                                })
                                println("-------------------------")
                            }
                            Seq()
                        case (category, ms) => Seq(MethodCategoryChoice(bt, category, ms))
                    }

                    // t是枚举类型，则添加枚举选项
                    val enumChoice = methodEntityRepository.getType(t) match {
                        case e: EnumEntity =>
                            EnumChoice(e) :: Nil
                        case _ =>
                            Nil
                    }

                    val simpleName = CodeUtil.qualifiedClassName2Simple(t).toLowerCase
                    val q = s"Which $simpleName?"
                    ChoiceQuestion(q, vars.toSeq.map(VariableChoice.apply) ++ enumChoice ++ methodCategoryChoices)
                }
            case at: ArrayType =>
                val vars = context.findVariables(at)

                val simpleName = CodeUtil.qualifiedClassName2Simple(at.componentType.toString).toLowerCase
                val q = s"Which ${simpleName}s?"
                ChoiceQuestion(q, vars.toSeq.map(VariableChoice.apply) :+ CreateArrayChoice(at))
        }
    }
}
