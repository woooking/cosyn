package com.github.woooking.cosyn.code.model

import scala.reflect.ClassTag

class ASTCollector {
    def collect[F : ClassTag](node: Node): List[F] = {
        val list: List[F] = node match {
            case f: F => f :: Nil
            case _ => Nil
        }

        (list /: node.children) ((l, n) => l ++ collect(n))
    }
}

