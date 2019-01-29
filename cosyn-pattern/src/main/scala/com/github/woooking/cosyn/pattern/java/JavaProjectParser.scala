package com.github.woooking.cosyn.pattern.java

import java.nio.file.Path

import better.files.File
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, Parameter}
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.{CombinedTypeSolver, JavaParserTypeSolver}
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.javaparser.utils.SourceRoot
import com.github.javaparser.{JavaParser, ParseResult, ParserConfiguration}
import com.github.woooking.cosyn.pattern.CosynConfig
import com.github.woooking.cosyn.pattern.api.Pipeline
import com.github.woooking.cosyn.pattern.api.Pipeline.Filter
import com.github.woooking.cosyn.pattern.dfgprocessor.cfg.CFGImpl
import com.github.woooking.cosyn.pattern.dfgprocessor.dfg.SimpleDFG
import com.github.woooking.cosyn.pattern.dfgprocessor.ir.IRArg

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.reflect.runtime.universe._

class JavaProjectParser extends Pipeline[Path, Seq[SimpleDFG]] {
    private val parserConfiguration = new ParserConfiguration
    parserConfiguration.setSymbolResolver(new JavaSymbolSolver(new CombinedTypeSolver(
        CosynConfig.srcCodeDirs.map(new JavaParserTypeSolver(_)): _*
    )))

    JavaParser.setStaticConfiguration(parserConfiguration)

    type SourceRoots = Seq[SourceRoot]
    type CUResult = ParseResult[CompilationUnit]
    type CUs = Seq[CompilationUnit]
    type CFGs = Seq[CFGImpl]
    type DFGs = Seq[SimpleDFG]

    private[this] val sourceRootFilters = mutable.ArrayBuffer[Filter[SourceRoots]]()
    private[this] val cuFilters = mutable.ArrayBuffer[Filter[CUs]]()
    private[this] val cfgFilters = mutable.ArrayBuffer[Filter[CFGs]]()
    private[this] val dfgFilters = mutable.ArrayBuffer[Filter[DFGs]]()

    def register[T: TypeTag](filter: Filter[T]): this.type = {
        filter match {
            case f if typeOf[T] <:< typeOf[SourceRoots] => sourceRootFilters += f.asInstanceOf[Filter[SourceRoots]]
            case f if typeOf[T] <:< typeOf[CUs] => cuFilters += f.asInstanceOf[Filter[CUs]]
            case f if typeOf[T] <:< typeOf[CFGs] => cfgFilters += f.asInstanceOf[Filter[CFGs]]
            case f if typeOf[T] <:< typeOf[DFGs] => dfgFilters += f.asInstanceOf[Filter[DFGs]]
        }
        this
    }

    private def resolveParameterType(p: Parameter): String = p.getType.asString()

    private def sourceFilesGenerator: Pipeline[Path, Seq[CompilationUnit]] =
        (path: Path) => File(path).listRecursively
            .filter(_.extension.contains(".java"))
            .map(_.toJava)
            .map(JavaParser.parse)
            .toSeq

    private def sourceRootsGenerator: Pipeline[Path, SourceRoots] =
        (path: Path) => new SymbolSolverCollectionStrategy().collect(path).getSourceRoots.asScala

    private def sourceRootFilter: Filter[SourceRoots] = (Pipeline.id[SourceRoots] /: sourceRootFilters) (_ | _)

    private def javaParser: Pipeline[SourceRoots, CUs] =
        (sourceRoots: Seq[SourceRoot]) => sourceRoots
            .flatMap(_.tryToParseParallelized().asScala)
            .filter(_.isSuccessful)
            .map(_.getResult.get())

    private def cuResultFilter: Filter[CUs] = (Pipeline.id[CUs] /: cuFilters) (_ | _)

    private def cuResult2cfg: Pipeline[CUs, Seq[CFGImpl]] =
        (cus: CUs) => cus
            .flatMap(cu => cu.findAll(classOf[ConstructorDeclaration]).asScala ++ cu.findAll(classOf[MethodDeclaration]).asScala)
            .map(method => {
                val cfg = new CFGImpl("", s"${method.getSignature.asString()}", method)
                val statementVisitor = new JavaStatementVisitor(cfg)

                val body = method match {
                    case decl: ConstructorDeclaration => decl.getBody
                    case decl: MethodDeclaration if decl.getBody.isPresent => decl.getBody.get()
                    case _: MethodDeclaration => new BlockStmt
                }

                method.getParameters.forEach(p => cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, resolveParameterType(p))))
                val pair = body.accept(statementVisitor, cfg.createContext(cfg.entry))
                pair.block.seal()
                pair.block.setNext(cfg.exit)
                cfg
            })

    private def cfgFilter: Filter[CFGs] = (Pipeline.id[CFGs] /: cfgFilters) (_ | _)

    private def cfg2dfg: Pipeline[CFGs, DFGs] = (cfgs: CFGs) => cfgs.map(SimpleDFG.apply)

    private def dfgFilter: Filter[DFGs] = (Pipeline.id[DFGs] /: dfgFilters) (_ | _)

    override def >>:(input: Path): DFGs =
    //        input >>: (sourceRootsGenerator | sourceRootFilter | javaParser | cuResultFilter | cuResult2cfg | cfgFilter | cfg2dfg | dfgFilter)
        input >>: (sourceFilesGenerator | cuResultFilter | cuResult2cfg | cfgFilter | cfg2dfg | dfgFilter)


}
