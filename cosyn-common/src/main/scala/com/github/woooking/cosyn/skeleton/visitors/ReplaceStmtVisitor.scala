package com.github.woooking.cosyn.skeleton.visitors

import com.github.woooking.cosyn.skeleton.model.Type
import com.github.woooking.cosyn.skeleton.model.{BlockStmt, Statement}
import shapeless.{:+:, ::, CNil, Coproduct, Generic, HList, HNil, Inl, Inr, Lazy}

trait ReplaceStmtVisitor[T] {
    def replace(node: T, block: BlockStmt, oldStmt: Statement, newStmts: Seq[Statement]): Option[T]
}

object ReplaceStmtVisitor {
    type RV[T] = ReplaceStmtVisitor[T]

    def instance[A](implicit enc: RV[A]): RV[A] = enc

    def create[A](func: (A, BlockStmt, Statement, Seq[Statement]) => Option[A]): RV[A] = func(_, _, _, _)

    def nil[A]: RV[A] = create((_, _, _, _) => None)

    implicit def valInstance[V <: AnyVal]: RV[V] = nil

    implicit def stringInstance: RV[String] = nil

    //    implicit def stmtInstance[R](implicit generic: Generic.Aux[Statement, R], lazyInstance: Lazy[RV[R]]): RV[Statement] = create { (value, block, oldStmt, newStmts) =>
    //        value match {
    //            case b: BlockStmt if b == block =>
    //                val (front, end) = b.statements.span(_ != oldStmt)
    //                Some(b.copy(statements = front ++ newStmts ++ end.tail))
    //            case v =>
    //                lazyInstance.value.replace(generic.to(v), block, oldStmt, newStmts).map(generic.from)
    //        }
    //    }

    implicit def stmtInstance[R](implicit generic: Generic.Aux[BlockStmt, R], lazyInstance: Lazy[RV[R]]): RV[BlockStmt] = create { (value, block, oldStmt, newStmts) =>
        if (value == block) {
            val (front, end) = value.statements.span(_ != oldStmt)
            Some(value.copy(statements = front ++ newStmts ++ end.tail))
        } else {
            lazyInstance.value.replace(generic.to(value), block, oldStmt, newStmts).map(generic.from)
        }
    }

    implicit def seqInstance[V](implicit vInstance: Lazy[RV[V]]): RV[Seq[V]] = create { (seq, block, oldStmt, newStmts) =>
        val newSeq = seq.zipWithIndex.map { case (v, index) => index -> vInstance.value.replace(v, block, oldStmt, newStmts) }
        newSeq.find(_._2.isDefined) match {
            case None => None
            case Some((_, None)) => throw new Exception("")
            case Some((index, Some(filled))) => Some(seq.updated(index, filled))
        }
    }

    implicit def typeInstance[T <: Type]: RV[T] = nil

    implicit def optionInstance[V](implicit vInstance: Lazy[RV[V]]): RV[Option[V]] = create {
        case (None, _, _, _) => None
        case (Some(v), block, oldStmt, newStmts) => vInstance.value.replace(v, block, oldStmt, newStmts).map(Some(_))
    }

    implicit def hNilInstance: RV[HNil] = nil

    implicit def hListInstance[H, T <: HList](implicit hInstance: Lazy[RV[H]], tInstance: RV[T]): RV[H :: T] = create {
        case (h :: t, block, oldStmt, newStmts) =>
            hInstance.value.replace(h, block, oldStmt, newStmts) match {
                case Some(v1) =>
                    Some(v1 :: t)
                case None =>
                    tInstance.replace(t, block, oldStmt, newStmts).map(h :: _)
            }
    }

    implicit def cNilInstance: RV[CNil] = nil

    implicit def coproductInstance[H, T <: Coproduct](implicit hInstance: Lazy[RV[H]], tInstance: RV[T]): RV[H :+: T] = create {
        case (Inl(h), block, oldStmt, newStmts) => hInstance.value.replace(h, block, oldStmt, newStmts).map(Inl(_))
        case (Inr(t), block, oldStmt, newStmts) => tInstance.replace(t, block, oldStmt, newStmts).map(Inr(_))
    }

    implicit def genericInstance[A, R](implicit generic: Generic.Aux[A, R], rInstance: Lazy[RV[R]]): RV[A] = create { (value, block, oldStmt, newStmts) =>
        rInstance.value.replace(generic.to(value), block, oldStmt, newStmts).map(generic.from)
    }
}