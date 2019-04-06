package com.github.woooking.cosyn.comm.skeleton.visitors

import com.github.woooking.cosyn.comm.skeleton.model.{Type, _}
import org.slf4s.Logging
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait ReplaceVisitor[T, N] {
    def replace(node: T, old: N, n: N): Option[T]
}

object ReplaceVisitor extends Logging {
    type RV[T, N] = ReplaceVisitor[T, N]

    def replace[N](node: BlockStmt, old: N, n: N): BlockStmt = {
        instance[BlockStmt, N].replace(node, old, n) match {
            case Some(value) => value
            case None => node
        }
    }

    private def instance[A, B](implicit enc: RV[A, B]): RV[A, B] = enc

    private def create[A, N](func: (A, N, N) => Option[A]): RV[A, N] = func(_, _, _)

    private def nil[A, N]: RV[A, N] = create((_, _, _) => None)

    private implicit def valInstance[V <: AnyVal, N]: RV[V, N] = nil

    private implicit def stringInstance[N]: RV[String, N] = nil

    private implicit def seqInstance[V, N](implicit vInstance: Lazy[RV[V, N]]): RV[Seq[V], N] = create { (seq, old, n) =>
        val newSeq = seq.zipWithIndex.map { case (v, index) => index -> vInstance.value.replace(v, old, n) }
        newSeq.find(_._2.isDefined) match {
            case None => None
            case Some((_, None)) => throw new Exception("")
            case Some((index, Some(filled))) => Some(seq.updated(index, filled))
            case v =>
                log.error(s"Match error here, $v not matched")
                ???
        }
    }

    private implicit def typeInstance[T <: Type, N]: RV[T, N] = nil

    private implicit def optionInstance[V, N](implicit vInstance: Lazy[RV[V, N]]): RV[Option[V], N] = create {
        case (None, _, _) => None
        case (Some(v), old, n) => vInstance.value.replace(v, old, n).map(Some(_))
    }

    private implicit def hNilInstance[N]: RV[HNil, N] = nil

    private implicit def hListInstance[H, T <: HList, N](implicit hInstance: Lazy[RV[H, N]], tInstance: RV[T, N]): RV[H :: T, N] = create {
        case (h :: t, old, n) =>
            hInstance.value.replace(h, old, n) match {
                case Some(v1) =>
                    Some(v1 :: t)
                case None =>
                    tInstance.replace(t, old, n).map(h :: _)
            }
    }

    private implicit def cNilInstance[N]: RV[CNil, N] = nil

    private implicit def coproductInstance[H, T <: Coproduct, N](implicit hInstance: Lazy[RV[H, N]], tInstance: RV[T, N]): RV[H :+: T, N] = create {
        case (Inl(h), old, n) => hInstance.value.replace(h, old, n).map(Inl(_))
        case (Inr(t), old, n) => tInstance.replace(t, old, n).map(Inr(_))
    }

    private implicit def genericInstance[A, R, N](implicit generic: Generic.Aux[A, R], rInstance: Lazy[RV[R, N]]): RV[A, N] = create { (value, old, n) =>
        value match {
            case _ if value == old =>
                Some(n.asInstanceOf[A])
            case _ =>
                rInstance.value.replace(generic.to(value), old, n).map(generic.from)
        }
    }
}
