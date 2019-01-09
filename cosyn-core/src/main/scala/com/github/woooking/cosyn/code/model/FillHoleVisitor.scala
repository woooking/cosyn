package com.github.woooking.cosyn.code.model

import com.github.woooking.cosyn.code.model.ty.Type
import shapeless.{:+:, ::, <:!<, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait FillHoleVisitor[T] {
    def fill(node: T, hole: HoleExpr, expr: Expression): Option[T]
}

object FillHoleVisitor {
    type FV[T] = FillHoleVisitor[T]

    def apply[A](implicit enc: FV[A]): FV[A] = enc

    def instance[A](func: (A, HoleExpr, Expression) => Option[A]): FV[A] = (node: A, hole: HoleExpr, expr: Expression) => func(node, hole, expr)

    def nil[A]: FV[A] = instance((_, _, _) => None)

    implicit def valInstance[V <: AnyVal]: FV[V] = nil

    implicit def stringInstance: FV[String] = nil

    implicit def exprInstance[R](implicit generic: Generic.Aux[Expression, R], lazyInstance: Lazy[FV[R]]): FV[Expression] = instance { (value, hole, expr) =>
        value match {
            case h: HoleExpr if h == hole =>
                Some(expr)
            case v =>
                lazyInstance.value.fill(generic.to(v), hole, expr).map(generic.from)
        }
    }

    implicit def seqInstance[V](implicit vInstance: Lazy[FV[V]]): FV[Seq[V]] = instance { (seq, hole, expr) =>
        val newSeq = seq.zipWithIndex.map { case (v, index) => index -> vInstance.value.fill(v, hole, expr) }
        newSeq.find(_._2.isDefined) match {
            case None => None
            case Some((_, None)) => throw new Exception("")
            case Some((index, Some(filled))) => Some(seq.updated(index, filled))
        }
    }

    implicit def typeInstance[T <: Type]: FV[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[FV[V]]): FV[Option[V]] = instance {
        case (None, _, _) => None
        case (Some(v), hole, expr) => vInstance.value.fill(v, hole, expr).map(Some(_))
    }

    implicit def hNilInstance: FV[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[FV[H]], tInstance: FV[T]): FV[H :: T] = instance {
        case (h :: t, hole, expr) =>
            hInstance.value.fill(h, hole, expr) match {
                case Some(v1) =>
                    Some(v1 :: t)
                case None =>
                    tInstance.fill(t, hole, expr).map(h :: _)
            }
    }

    implicit def cNilInstance: FV[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[FV[H]], tInstance: FV[T]): FV[H :+: T] = instance {
        case (Inl(h), hole, expr) => hInstance.value.fill(h, hole, expr).map(Inl(_))
        case (Inr(t), hole, expr) => tInstance.fill(t, hole, expr).map(Inr(_))
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[FV[R]], ev: A <:!< Expression): FV[A] = instance { (value, hole, expr) =>
        rInstance.value.fill(generic.to(value), hole, expr).map(generic.from)
    }
}
