package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model.{HoleExpr, Node, Type}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait DiffVisitor[T] {
    def collect(value: T): List[HoleExpr]
}

object DiffVisitor {
    type HC[T] = HoleCollector[T]

    def instance[A](implicit enc: HC[A]): HC[A] = enc

    def create[A](func: A => List[HoleExpr]): HC[A] = (value: A) => func(value)

    def nil[A]: HC[A] = create(_ => Nil)

    implicit def valInstance[V <: AnyVal]: HC[V] = nil

    implicit def stringInstance: HC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[HC[V]]): HC[Seq[V]] = create { seq =>
        (List.empty[HoleExpr] /: seq) ((l, v) => l ++ vInstance.value.collect(v))
    }

    implicit def typeInstance[T <: Type]: HC[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[HC[V]]): HC[Option[V]] = create {
        case None => Nil
        case Some(v) => vInstance.value.collect(v)
    }

    implicit def hNilInstance: HC[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :: T] = create {
        case h :: t =>
            hInstance.value.collect(h) ++ tInstance.collect(t)
    }

    implicit def cNilInstance: HC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :+: T] = create {
        case Inl(h) => hInstance.value.collect(h)
        case Inr(t) => tInstance.collect(t)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[HC[R]]): HC[A] = create {
        case d: HoleExpr =>
            d :: Nil
        case value =>
            rInstance.value.collect(generic.to(value))
    }
}