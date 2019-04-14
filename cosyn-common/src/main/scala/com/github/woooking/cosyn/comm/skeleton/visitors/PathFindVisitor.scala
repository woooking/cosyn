package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model.{Node, Type}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

import scala.{:: => :-:}

trait PathFindVisitor[T] {
    def find(node: T, path: List[Node]): Option[Node]
}

object PathFindVisitor {
    type HC[T] = PathFindVisitor[T]

    def instance[A](implicit enc: HC[A]): HC[A] = enc

    def create[A](func: (A, List[Node]) => Option[Node]): HC[A] = (node: A, path: List[Node]) => func(node, path)

    def nil[A]: HC[A] = create((_, _) => None)

    implicit def valInstance[V <: AnyVal]: HC[V] = nil

    implicit def stringInstance: HC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[HC[V]]): HC[Seq[V]] = create { (node, path) =>
        (Option.empty[Node] /: node) {
            case (None, v) => vInstance.value.find(v, path)
            case (l, _) => l
        }
    }

    implicit def typeInstance[T <: Type]: HC[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[HC[V]]): HC[Option[V]] = create {
        case (None, _) => None
        case (Some(v), path) => vInstance.value.find(v, path)
    }

    implicit def hNilInstance: HC[HNil] = nil

    implicit def hListInstance[H, T <: HList, R](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :: T] = create {
        case (h :: _, _ :-: Nil) if h.isInstanceOf[Node] =>
            Some(h.asInstanceOf[Node])
        case (h :: _, m :-: Nil) if h != m =>
            None
        case (h :: _, _ :-: t) =>
            hInstance.value.find(h, t)
    }

    implicit def cNilInstance: HC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :+: T] = create {
        case (Inl(h), path) => hInstance.value.find(h, path)
        case (Inr(t), path) => tInstance.find(t, path)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[HC[R]]): HC[A] = create {
        case (v, l) =>
            rInstance.value.find(generic.to(v), l)
    }
}