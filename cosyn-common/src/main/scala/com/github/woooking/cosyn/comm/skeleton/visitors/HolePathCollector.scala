package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model._
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait HolePathCollector[T] {
    def collect(value: T, hole: HoleExpr): List[Node]
}

object HolePathCollector {
    type HC[T] = HolePathCollector[T]

    def instance[A](implicit enc: HC[A]): HC[A] = enc

    def create[A](func: (A, HoleExpr) => List[Node]): HC[A] = (value: A, hole: HoleExpr) => func(value, hole)

    def nil[A]: HC[A] = create((_, _) => Nil)

    implicit def valInstance[V <: AnyVal]: HC[V] = nil

    implicit def stringInstance: HC[String] = nil

    implicit def seqInstance[V](implicit vInstance: Lazy[HC[V]]): HC[Seq[V]] = create { (seq, hole) =>
        (List.empty[Node] /: seq) {
            case (Nil, v) => vInstance.value.collect(v, hole)
            case (l, _) => l
        }
    }

    implicit def typeInstance[T <: Type]: HC[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[HC[V]]): HC[Option[V]] = create {
        case (None, _) => Nil
        case (Some(v), hole) => vInstance.value.collect(v, hole)
    }

    implicit def hNilInstance: HC[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :: T] = create {
        case (h :: t, hole) =>
            hInstance.value.collect(h, hole) match {
                case Nil => tInstance.collect(t, hole)
                case l => l
            }
    }

    implicit def cNilInstance: HC[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[HC[H]], tInstance: HC[T]): HC[H :+: T] = create {
        case (Inl(h), hole) if h.isInstanceOf[Node] => h match {
            case v: HoleExpr if v == hole =>
                v :: Nil
            case _ =>
                hInstance.value.collect(h, hole) match {
                    case Nil => Nil
                    case l => h.asInstanceOf[Node] :: l
                }
        }
        case (Inl(h), hole) => hInstance.value.collect(h, hole)
        case (Inr(t), hole) => tInstance.collect(t, hole)
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[HC[R]]): HC[A] = create {
        case (v, hole) =>
            rInstance.value.collect(generic.to(v), hole)
    }
}