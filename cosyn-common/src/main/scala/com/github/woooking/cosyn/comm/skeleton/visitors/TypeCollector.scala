package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model.Type
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait TypeCollector[T] {
    def collect(value: T): Set[Type]
}

object TypeCollector {
    type TC[T] = TypeCollector[T]

    def instance[A](implicit enc: TC[A]): TC[A] = enc

    def create[A](func: A => Set[Type]): TC[A] = (value: A) => func(value)

    def nil[A]: TC[A] = create(_ => Set.empty)

    implicit def valInstance[V <: AnyVal]: TC[V] = nil

    implicit def stringInstance: TC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[TC[V]]): TC[Seq[V]] = create { seq =>
        (Set.empty[Type] /: seq) ((l, v) => l ++ vInstance.value.collect(v))
    }

    implicit def typeInstance[T <: Type]: TC[T] = create(t => Set(t))

    implicit def optionInstance[V](implicit vInstance: Lazy[TC[V]]): TC[Option[V]] = create {
        case None => Set.empty
        case Some(v) => vInstance.value.collect(v)
    }

    implicit def hNilInstance: TC[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[TC[H]], tInstance: TC[T]): TC[H :: T] = create {
        case h :: t =>
            hInstance.value.collect(h) ++ tInstance.collect(t)
    }

    implicit def cNilInstance: TC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[TC[H]], tInstance: TC[T]): TC[H :+: T] = create {
        case Inl(h) => hInstance.value.collect(h)
        case Inr(t) => tInstance.collect(t)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[TC[R]]): TC[A] = create(value =>
        rInstance.value.collect(generic.to(value))
    )
}