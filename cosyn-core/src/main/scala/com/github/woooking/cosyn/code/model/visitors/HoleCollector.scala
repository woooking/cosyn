package com.github.woooking.cosyn.code.model.visitors

import com.github.woooking.cosyn.code.model.{HoleExpr, Node, Type}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait HoleCollector[T] {
    def collect(value: T): List[HoleExpr]
}

object HoleCollector {
    type HC[T] = HoleCollector[T]

    def instance[A](implicit enc: HC[A]): HC[A] = enc

    def create[A](func: A => List[HoleExpr]): HC[A] = (value: A) => func(value)

    def nil[A]: HC[A] = create(_ => Nil)

    implicit def typeInstance[T <: Type]: HC[T] = nil

    implicit def hNilInstance: HC[HNil] = nil

    implicit def hListNodeInstance[H <: Node, T <: HList](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :: T] = create {
        case h :: t =>
            hInstance.value.collect(h) ++ tInstance.collect(t)
    }

    implicit def hListSeqInstance[V, S <: Seq[V], T <: HList](implicit vInstance: Lazy[HC[V]], tInstance: HC[T]): HC[S :: T] = create {
        case s :: t =>
            s.flatMap(vInstance.value.collect).toList ++ tInstance.collect(t)
    }

    implicit def hListOptionInstance[V, T <: HList](implicit vInstance: Lazy[HC[V]], tInstance: HC[T]): HC[Option[V] :: T] = create {
        case None :: t =>
            tInstance.collect(t)
        case Some(v) :: t =>
            vInstance.value.collect(v) ++ tInstance.collect(t)
    }

    implicit def hListInstance[H, T <: HList](implicit tInstance: HC[T]): HC[H :: T] = create {
        case _ :: t =>
            tInstance.collect(t)
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