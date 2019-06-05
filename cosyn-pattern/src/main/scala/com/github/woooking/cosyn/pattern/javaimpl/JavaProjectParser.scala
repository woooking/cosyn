package com.github.woooking.cosyn.pattern.javaimpl

import java.nio.file.Path

import better.files.File
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{ConstructorDeclaration, MethodDeclaration, Parameter}
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.{CombinedTypeSolver, JavaParserTypeSolver}
import com.github.javaparser.{JavaParser, ParseResult, ParserConfiguration}
import com.github.woooking.cosyn.comm.util.FunctionUtil
import com.github.woooking.cosyn.pattern.CosynConfig
import com.github.woooking.cosyn.pattern.api.Pipe
import com.github.woooking.cosyn.pattern.api.Pipe.Filter
import com.github.woooking.cosyn.pattern.javaimpl.cfg.CFG
import com.github.woooking.cosyn.pattern.javaimpl.dfg.SimpleDFG
import com.github.woooking.cosyn.pattern.javaimpl.ir.IRArg
import org.slf4s.Logging

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.parallel._
import scala.reflect.runtime.universe._

class JavaProjectParser extends Pipe[Path, Seq[SimpleDFG]] with Logging {
    private val parserConfiguration = new ParserConfiguration
    parserConfiguration.setSymbolResolver(new JavaSymbolSolver(new CombinedTypeSolver(
        CosynConfig.global.srcCodeDirs.map(_.path).map(new JavaParserTypeSolver(_)): _*
    )))
    private val javaParser = new JavaParser(parserConfiguration)

    type CUResult = ParseResult[CompilationUnit]
    type CUs = Seq[CompilationUnit]
    type CFGs = Seq[CFG]
    type DFGs = Seq[SimpleDFG]

    private[this] val cuFilters = mutable.ArrayBuffer[Filter[CUs]]()
    private[this] val cfgFilters = mutable.ArrayBuffer[Filter[CFGs]]()
    private[this] val dfgFilters = mutable.ArrayBuffer[Filter[DFGs]]()

    def register[T: TypeTag](filter: Filter[T]): this.type = {
        filter match {
            case f if typeOf[T] <:< typeOf[CUs] => cuFilters += f.asInstanceOf[Filter[CUs]]
            case f if typeOf[T] <:< typeOf[CFGs] => cfgFilters += f.asInstanceOf[Filter[CFGs]]
            case f if typeOf[T] <:< typeOf[DFGs] => dfgFilters += f.asInstanceOf[Filter[DFGs]]
        }
        this
    }

    private def resolveParameterType(p: Parameter): Option[String] = {
        scala.util.Try(p.getType.resolve().describe()).toOption
    }

    private def parse(file: java.io.File): Option[CompilationUnit] = {
        try {
            val result = javaParser.parse(file)
            if (result.isSuccessful) Some(result.getResult.get())
            else {
                result.getProblems.forEach(_.getCause.ifPresent(log.error(s"Parse ${file.getAbsolutePath} error", _)))
                None
            }
        } catch {
            case e: Throwable =>
                log.error(s"Parse ${file.getAbsolutePath} error, ${e.getMessage}")
                None
        }
    }

    private def sourceFilesGenerator: Pipe[Path, Seq[CompilationUnit]] =
        (path: Path) => {
            val files =
                File(path).listRecursively
                    .toSeq
                    //                    .par
                    .filter(_.extension.contains(".java"))
                    //            .filter(!_.path.iterator().asScala.map(_.toString).contains("test"))
                    .filter(_.contentAsString.contains(s"import ${CosynConfig.global.classFullQualifiedName}"))

            log.info(s"文件数量: ${files.size}")

            files.map(_.toJava)
                .map(parse)
                .flatMap(_.toSeq)
            //                .seq
        }

    private def cuResult2cfg: Pipe[CUs, Seq[CFG]] =
        (cus: CUs) => {
            val cfgs = cus.flatMap(cu => cu.findAll(classOf[ConstructorDeclaration]).asScala ++ cu.findAll(classOf[MethodDeclaration]).asScala)
                .flatMap(method => {
                    try {
                        val cfg = new CFG(s"${method.getSignature.asString()}", method)
                        val body = method match {
                            case decl: ConstructorDeclaration => decl.getBody
                            case decl: MethodDeclaration if decl.getBody.isPresent => decl.getBody.get()
                            case _: MethodDeclaration => new BlockStmt
                        }
                        method.getParameters.forEach(p =>
                            for (ty <- resolveParameterType(p)) cfg.writeVar(p.getName.getIdentifier, cfg.entry, IRArg(p.getName.getIdentifier, ty))
                        )
                        val statementVisitor = new JavaStatementVisitor(cfg)
                        val pair = body.accept(statementVisitor.visitor, statementVisitor.outerCfg.Context(cfg.entry))
                        pair.block.seal()
                        pair.block.setNext(cfg.exit)
                        Seq(cfg)
                    } catch {
                        case e: Throwable =>
                            log.error("Cfg generate error", e)
                            Seq()
                    }
                })
            log.info(s"控制流图数: ${cfgs.size}")
            cfgs
        }

    private def cfg2dfg: Pipe[CFGs, DFGs] = (cfgs: CFGs) => cfgs.map(SimpleDFG.apply)

    private def cuResultFilter: Filter[CUs] = FunctionUtil.sum(cuFilters.toList)

    private def cfgFilter: Filter[CFGs] = FunctionUtil.sum(cfgFilters.toList)

    private def dfgFilter: Filter[DFGs] = FunctionUtil.sum(dfgFilters.toList)

    override def >>:(input: Path): DFGs =
        input >>: (sourceFilesGenerator | cuResultFilter | cuResult2cfg | cfgFilter | cfg2dfg | dfgFilter)


}
