package com.github.woooking.cosyn.code.model

import com.github.woooking.cosyn.code.model.ty.Type
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait ParentCollector[T] {
    def collect(parent: Node, value: T): Map[Node, Node]
}

object ParentCollector {
    type PC[T] = ParentCollector[T]

    def apply[A](implicit enc: PC[A]): PC[A] = enc

    def instance[A](func: (Node, A) => Map[Node, Node]): PC[A] = (parent: Node, value: A) => func(parent, value)

    def nil[A]: PC[A] = instance((_, _) => Map())

    implicit def valInstance[V <: AnyVal]: PC[V] = nil

    implicit def stringInstance: PC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[PC[V]]): PC[Seq[V]] = instance { (parent, seq) =>
        (Map.empty[Node, Node] /: seq) ((l, v) => l ++ vInstance.value.collect(parent, v))
    }

    implicit def typeInstance[T <: Type]: PC[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[PC[V]]): PC[Option[V]] = instance {
        case (_, None)=> Map.empty
        case (parent, Some(v)) => vInstance.value.collect(parent, v)
    }

    implicit def hNilInstance: PC[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[PC[H]], tInstance: PC[T]): PC[H :: T] = instance {
        case (parent, h :: t) =>
            hInstance.value.collect(parent, h) ++ tInstance.collect(parent, t)
    }

    implicit def cNilInstance: PC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[PC[H]], tInstance: PC[T]): PC[H :+: T] = instance {
        case (parent, Inl(h)) => hInstance.value.collect(parent, h)
        case (parent, Inr(t)) => tInstance.collect(parent, t)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[PC[R]]): PC[A] = instance { (parent, value) =>
        value match {
            case n: Node =>
                rInstance.value.collect(n, generic.to(value)) + (n -> parent)
            case _ => Map.empty
        }
    }
}

