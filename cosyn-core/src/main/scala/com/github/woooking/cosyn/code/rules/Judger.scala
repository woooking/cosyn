package com.github.woooking.cosyn.code.rules

trait Judger[T] {
    type Rule = T => Boolean

    def rules: Seq[Rule]

    def judge(value: T): Boolean
}

trait PositiveJudger[T] extends Judger[T] {
    override final def judge(value: T): Boolean = {
        (false /: rules) ((s, r) => s || r(value))
    }
}

trait NegativeJudger[T] extends Judger[T] {
    override final def judge(value: T): Boolean = {
        (true /: rules) ((s, r) => s && r(value))
    }
}