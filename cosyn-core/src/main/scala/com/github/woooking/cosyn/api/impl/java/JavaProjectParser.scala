package com.github.woooking.cosyn.api.impl.java

import java.nio.file.Path

import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, Parameter}
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.dfgprocessor.cfg.{CFG, CFGImpl}
import com.github.woooking.cosyn.dfgprocessor.ir.IRArg

import scala.collection.JavaConverters._

object JavaProjectParser {
    def resolveParameterType(p: Parameter): String = p.getType.asString()

    def parse(path: Path): Seq[CFGImpl] = {
        val projectRoot = new SymbolSolverCollectionStrategy().collect(path)

        projectRoot.getSourceRoots.asScala
            .flatMap(_.tryToParseParallelized().asScala)
            .filter(_.isSuccessful)
            .map(_.getResult.get())
            .flatMap(cu => cu.findAll(classOf[ConstructorDeclaration]).asScala ++ cu.findAll(classOf[MethodDeclaration]).asScala)
            .map(method => {
                val cfg = new CFGImpl("", s"${method.getSignature.asString()}")
                val statementVisitor = new JavaStatementVisitor(cfg)

                val body = method match {
                    case decl: ConstructorDeclaration => decl.getBody
                    case decl: MethodDeclaration => decl.getBody.get()
                }

                method.getParameters.forEach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, resolveParameterType(p))))
                val pair = body.accept(statementVisitor, cfg.createContext(cfg.entry))
                pair.block.seal()
                pair.block.setNext(cfg.exit)
                cfg
            })
    }
}
