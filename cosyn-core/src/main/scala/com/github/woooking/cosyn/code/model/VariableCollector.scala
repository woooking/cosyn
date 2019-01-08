package com.github.woooking.cosyn.code.model

import com.github.woooking.cosyn.code.model.ty.Type
import shapeless._

trait VariableCollector[T] {
    def collect(value: T): List[(String, Type)]
}

object VariableCollector {
    type VC[T] = VariableCollector[T]

    def apply[A](implicit enc: VC[A]): VC[A] = enc

    def instance[A](func: A => List[(String, Type)]): VC[A] = (value: A) => func(value)

    def nil[A]: VC[A] = instance(_ => Nil)

    implicit def valInstance[V <: AnyVal]: VC[V] = nil

    implicit def stringInstance: VC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[VC[V]]): VC[Seq[V]] = instance { seq =>
        (List.empty[(String, Type)] /: seq) ((l, v) => l ++ vInstance.value.collect(v))
    }

    implicit def typeInstance[T <: Type]: VC[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[VC[V]]): VC[Option[V]] = instance {
        case None => Nil
        case Some(v) => vInstance.value.collect(v)
    }

    implicit def hNilInstance: VC[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[VC[H]], tInstance: VC[T]): VC[H :: T] = instance {
        case h :: t =>
            hInstance.value.collect(h) ++ tInstance.collect(t)
    }

    implicit def cNilInstance: VC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[VC[H]], tInstance: VC[T]): VC[H :+: T] = instance {
        case Inl(h) => hInstance.value.collect(h)
        case Inr(t) => tInstance.collect(t)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[VC[R]]): VC[A] = instance { value =>
        rInstance.value.collect(generic.to(value))
    }
}
