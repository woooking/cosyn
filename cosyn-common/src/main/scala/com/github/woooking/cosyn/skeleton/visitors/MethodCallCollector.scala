package com.github.woooking.cosyn.skeleton.visitors

import com.github.woooking.cosyn.skeleton.model.{MethodCallExpr, Type, VariableDeclaration}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait MethodCallCollector[T] {
    def collect(value: T): List[String]
}

object MethodCallCollector {
    type MC[T] = MethodCallCollector[T]

    def instance[A](implicit enc: MC[A]): MC[A] = enc

    def create[A](func: A => List[String]): MC[A] = (value: A) => func(value)

    def nil[A]: MC[A] = create(_ => Nil)

    implicit def valInstance[V <: AnyVal]: MC[V] = nil

    implicit def stringInstance: MC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[MC[V]]): MC[Seq[V]] = create { seq =>
        (List.empty[String] /: seq) ((l, v) => l ++ vInstance.value.collect(v))
    }

    implicit def typeInstance[T <: Type]: MC[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[MC[V]]): MC[Option[V]] = create {
        case None => Nil
        case Some(v) => vInstance.value.collect(v)
    }

    implicit def hNilInstance: MC[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[MC[H]], tInstance: MC[T]): MC[H :: T] = create {
        case h :: t =>
            hInstance.value.collect(h) ++ tInstance.collect(t)
    }

    implicit def cNilInstance: MC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[MC[H]], tInstance: MC[T]): MC[H :+: T] = create {
        case Inl(h) => hInstance.value.collect(h)
        case Inr(t) => tInstance.collect(t)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[MC[R]]): MC[A] = create {
        case d: MethodCallExpr =>
            d.getQualifiedSignature :: Nil
        case value =>
            rInstance.value.collect(generic.to(value))
    }
}