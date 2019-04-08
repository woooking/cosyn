package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model.Type
import com.github.woooking.cosyn.comm.skeleton.model._
import org.slf4s.Logging
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy, the}

trait FillHoleVisitor[T] {
    def fill(node: T, hole: HoleExpr, expr: Expression): Option[T]
}

object FillHoleVisitor extends Logging {
    type FV[T] = FillHoleVisitor[T]

    def fillHole(scope: BlockStmt, hole: HoleExpr, expr: Expression): BlockStmt = {
        instance[BlockStmt].fill(scope, hole, expr) match {
            case Some(value) => value
            case None => scope
        }
    }

    def fillHole(scope: ForEachStmt, hole: HoleExpr, expr: Expression): ForEachStmt = {
        instance[ForEachStmt].fill(scope, hole, expr) match {
            case Some(value) => value
            case None => scope
        }
    }

    private def instance[A](implicit enc: FV[A]): FV[A] = enc

    private def create[A](func: (A, HoleExpr, Expression) => Option[A]): FV[A] = func(_, _, _)

    private def nil[A]: FV[A] = create((_, _, _) => None)

    private implicit def valInstance[V <: AnyVal]: FV[V] = nil

    private implicit def stringInstance: FV[String] = nil

    private implicit def exprInstance[R](implicit generic: Generic.Aux[Expression, R], lazyInstance: Lazy[FV[R]]): FV[Expression] = create { (value, hole, expr) =>
        value match {
            case h: HoleExpr if h == hole =>
                Some(expr)
            case v =>
                lazyInstance.value.fill(generic.to(v), hole, expr).map(generic.from)
        }
    }

    private implicit def seqInstance[V](implicit vInstance: Lazy[FV[V]]): FV[Seq[V]] = create { (seq, hole, expr) =>
        val newSeq = seq.zipWithIndex.map { case (v, index) => index -> vInstance.value.fill(v, hole, expr) }
        newSeq.find(_._2.isDefined) match {
            case None => None
            case Some((_, None)) => throw new Exception("")
            case Some((index, Some(filled))) => Some(seq.updated(index, filled))
            case v =>
                log.error(s"Match error here, $v not matched")
                ???
        }
    }

    private implicit def typeInstance[T <: Type]: FV[T] = nil

    private implicit def optionInstance[V](implicit vInstance: Lazy[FV[V]]): FV[Option[V]] = create {
        case (None, _, _) => None
        case (Some(v), hole, expr) => vInstance.value.fill(v, hole, expr).map(Some(_))
    }

    private implicit def hNilInstance: FV[HNil] = nil

    private implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[FV[H]], tInstance: FV[T]): FV[H :: T] = create {
        case (h :: t, hole, expr) =>
            hInstance.value.fill(h, hole, expr) match {
                case Some(v1) =>
                    Some(v1 :: t)
                case None =>
                    tInstance.fill(t, hole, expr).map(h :: _)
            }
    }

    private implicit def cNilInstance: FV[CNil] = nil

    private implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[FV[H]], tInstance: FV[T]): FV[H :+: T] = create {
        case (Inl(h), hole, expr) => hInstance.value.fill(h, hole, expr).map(Inl(_))
        case (Inr(t), hole, expr) => tInstance.fill(t, hole, expr).map(Inr(_))
    }

    private implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[FV[R]]): FV[A] = create { (value, hole, expr) =>
        rInstance.value.fill(generic.to(value), hole, expr).map(generic.from)
    }
}
