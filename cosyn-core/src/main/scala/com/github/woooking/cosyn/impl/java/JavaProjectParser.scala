package com.github.woooking.cosyn.impl.java

import java.nio.file.Path

import com.github.javaparser.ParseResult
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, Parameter}
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.SourceRoot
import com.github.woooking.cosyn.api.Pipeline
import com.github.woooking.cosyn.api.Pipeline.Filter
import com.github.woooking.cosyn.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.dfgprocessor.ir.IRArg

import scala.collection.JavaConverters._
import scala.collection.mutable

class JavaProjectParser extends Pipeline[Path, Seq[CFGImpl]] {
    type SourceRoots = Seq[SourceRoot]
    type CUResult = ParseResult[CompilationUnit]
    type CUResults = Seq[CUResult]
    type CFGs = Seq[CFGImpl]

    private[this] val sourceRootFilters = mutable.ArrayBuffer[Filter[SourceRoots]]()
    private[this] val cuResultFilters = mutable.ArrayBuffer[Filter[CUResults]]()
    private[this] val cfgFilters = mutable.ArrayBuffer[Filter[CFGs]]()

    def resolveParameterType(p: Parameter): String = p.getType.asString()

    private val sourceRootsGenerator: Pipeline[Path, SourceRoots] =
        (path: Path) => new SymbolSolverCollectionStrategy().collect(path).getSourceRoots.asScala

    private val sourceRootFilter: Filter[SourceRoots] = (Pipeline.id[SourceRoots] /: sourceRootFilters) (_ | _)

    private val javaParser: Pipeline[SourceRoots, CUResults] =
        (sourceRoots: Seq[SourceRoot]) => sourceRoots.flatMap(_.tryToParseParallelized().asScala)

    private val cuResultFilter: Filter[CUResults] = (Pipeline.id[CUResults] /: cuResultFilters) (_ | _)

    private val cuResult2CFG: Pipeline[CUResults, Seq[CFGImpl]] =
        (cus: CUResults) => cus.filter(_.isSuccessful)
            .map(_.getResult.get())
            .flatMap(cu => cu.findAll(classOf[ConstructorDeclaration]).asScala ++ cu.findAll(classOf[MethodDeclaration]).asScala)
            .map(method => {
                val cfg = new CFGImpl("", s"${method.getSignature.asString()}", method)
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

    private val cfgFilter: Filter[CFGs] = (Pipeline.id[CFGs] /: cfgFilters) (_ | _)

    override def >>:(input: Path): Seq[CFGImpl] =
        input >>: (sourceRootsGenerator | sourceRootFilter | javaParser | cuResultFilter | cuResult2CFG | cfgFilter)


}
