package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model.{Type, _}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait EndArrayInitVisitor[T] {
    def end(node: T, hole: HoleExpr): Option[T]
}

object EndArrayInitVisitor {
    type FV[T] = EndArrayInitVisitor[T]

    def end(node: BlockStmt, hole: HoleExpr): BlockStmt = {
        instance[BlockStmt].end(node, hole) match {
            case Some(value) => value
            case None => node
        }
    }

    private def instance[A](implicit enc: FV[A]): FV[A] = enc

    private def create[A](func: (A, HoleExpr) => Option[A]): FV[A] = func(_, _)

    private def nil[A]: FV[A] = create((_, _) => None)

    private implicit def valInstance[V <: AnyVal]: FV[V] = nil

    private implicit def stringInstance: FV[String] = nil

    private implicit def exprInstance[R](implicit generic: Generic.Aux[Expression, R], lazyInstance: Lazy[FV[R]]): FV[Expression] = create { (value, hole) =>
        value match {
            case e: ArrayCreationExpr if e.initializers.last == hole =>
                Some(e.copy(initializers = e.initializers.dropRight(1)))
            case v =>
                lazyInstance.value.end(generic.to(v), hole).map(generic.from)
        }
    }

    private implicit def seqInstance[V](implicit vInstance: Lazy[FV[V]]): FV[Seq[V]] = create { (seq, hole) =>
        val newSeq = seq.zipWithIndex.map { case (v, index) => index -> vInstance.value.end(v, hole) }
        newSeq.find(_._2.isDefined) match {
            case None => None
            case Some((_, None)) => throw new Exception("")
            case Some((index, Some(filled))) => Some(seq.updated(index, filled))
        }
    }

    private implicit def typeInstance[T <: Type]: FV[T] = nil

    private implicit def optionInstance[V](implicit vInstance: Lazy[FV[V]]): FV[Option[V]] = create {
        case (None, _) => None
        case (Some(v), hole) => vInstance.value.end(v, hole).map(Some(_))
    }

    private implicit def hNilInstance: FV[HNil] = nil

    private implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[FV[H]], tInstance: FV[T]): FV[H :: T] = create {
        case (h :: t, hole) =>
            hInstance.value.end(h, hole) match {
                case Some(v1) =>
                    Some(v1 :: t)
                case None =>
                    tInstance.end(t, hole).map(h :: _)
            }
    }

    private implicit def cNilInstance: FV[CNil] = nil

    private implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[FV[H]], tInstance: FV[T]): FV[H :+: T] = create {
        case (Inl(h), hole) => hInstance.value.end(h, hole).map(Inl(_))
        case (Inr(t), hole) => tInstance.end(t, hole).map(Inr(_))
    }

    private implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[FV[R]]): FV[A] = create { (value, hole) =>
        rInstance.value.end(generic.to(value), hole).map(generic.from)
    }
}
