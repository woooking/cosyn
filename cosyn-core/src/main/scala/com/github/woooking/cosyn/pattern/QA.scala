package com.github.woooking.cosyn.pattern

import com.github.woooking.cosyn.entity.MethodEntity
import com.github.woooking.cosyn.util.CodeUtil

trait QA

trait Choice

case class VariableChoice(name: String) extends Choice {
    override def toString: String = name
}

case class ConstructorChoice(ty: String, methods: Set[MethodEntity]) extends Choice {
    override def toString: String = s"A new ${CodeUtil.qualifiedClassName2Simple(ty)}"
}

case class WhichQA(question: String, choices: Seq[Choice]) extends QA {
    override def toString: String = {
        val choiceString = choices.zipWithIndex.map(p => s"${p._2 + 1}. ${p._1}").mkString("\n")
        s"$question\n$choiceString"
    }
}