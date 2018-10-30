package com.github.woooking.cosyn.api.impl

import java.nio.file.Path

import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, Parameter}
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.IRArg

import scala.collection.JavaConverters._

class JavaProjectParser(path: Path) {
    def resolveParameterType(p: Parameter): String = p.getType.asString()

    private val projectRoot = new SymbolSolverCollectionStrategy().collect(path)

    projectRoot.getSourceRoots.asScala.foreach(root => {
        val results = root.tryToParseParallelized()
        println(root.getRoot)
        results.forEach(result => {
            if (result.isSuccessful) {
                val cu = result.getResult.get()
                val methods = cu.findAll(classOf[ConstructorDeclaration]).asScala ++ cu.findAll(classOf[MethodDeclaration]).asScala
                val cfgs = methods.map(method => {
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
                println("success")
            } else {
                println("wrong")
            }
        })
    })


}
