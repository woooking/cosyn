package com.github.woooking.cosyn.macros

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.blackbox.Context

@compileTimeOnly("enable macro paradise to expand macro annotations")
class irdef extends StaticAnnotation {
    def macroTransform(annottees: Any*): Any = macro IR.irdefImpl
}

object IR {
    def irdefImpl(c: Context)(annottees: c.Expr[Any]*): c.Tree = {
        import c.universe._
        val inputs = annottees.map(_.tree).toList
        inputs match {
            case ClassDef(_, name, _, Template(_, _, body)) :: Nil =>
                println(body)
                println(body)
//                ClassDef(modifiers, name, tparams, Template(List(parent), self, body :+ q"init();"))
                q"""class $name (cfg: CFG, fromNodes: Set[NodeDelegate[_]]) extends IRDefStatement(cfg, fromNodes) {
                        override def uses: Seq[IRExpression] = Seq()
                    }"""
            case _ => throw new Exception()
        }
    }

}
