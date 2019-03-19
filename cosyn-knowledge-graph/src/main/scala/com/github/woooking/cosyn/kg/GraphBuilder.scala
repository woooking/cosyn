package com.github.woooking.cosyn.kg

import better.files.File.home
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body._
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.{JavaParserAnonymousClassDeclaration, JavaParserMethodDeclaration}
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.kg.entity.TypeEntity
import org.slf4s.Logging

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object GraphBuilder extends Logging {

    @tailrec
    private def getComponentTypeRecursively(ty: ResolvedType): ResolvedType = {
        ty match {
            case _ if ty.isArray => getComponentTypeRecursively(ty.asArrayType().getComponentType)
            case _ => ty
        }
    }

    private def type2entity(decl: ClassOrInterfaceType) = {
        val qualifiedName = decl.resolve().getQualifiedName
        EntityManager.getTypeEntityOrCreate(qualifiedName)
    }

    private def getIterableType(resolved: ResolvedReferenceTypeDeclaration): TypeEntity = resolved.getAllAncestors.asScala
        .find(_.getQualifiedName == "java.lang.Iterable")
        .map(_.getGenericParameterByName("T").get())
        .filter(_.isReferenceType)
        .map(_.asReferenceType().getQualifiedName)
        .map(EntityManager.getTypeEntityOrCreate).orNull

    def buildTypeMapping(typeDeclarations: Seq[TypeDeclaration[_]]): Unit = {
        typeDeclarations.map(_.resolve()).foreach(EntityManager.createTypeEntity)
    }

    def buildExtendRelation(classDecls: Seq[ClassOrInterfaceDeclaration]): Unit = {
        classDecls
            .foreach(decl => {
                val qualifiedName = decl.resolve().getQualifiedName
                val typeEntity = EntityManager.getTypeEntityOrCreate(qualifiedName)
                val parentTypes = decl.getExtendedTypes.asScala ++ decl.getImplementedTypes.asScala
                typeEntity.addExtendedTypes(parentTypes.map(type2entity).toSet.asJava)
                typeEntity.setIterableType(getIterableType(decl.resolve()))
            })
    }

    def buildMethodExtendRelation(cus: Seq[CompilationUnit]): Unit = {
        cus.flatMap(_.findAll(classOf[MethodDeclaration]).asScala)
            .foreach(decl => {
                try {
                    val resolvedMethod = decl.resolve().asInstanceOf[JavaParserMethodDeclaration]
                    resolvedMethod.getWrappedNode.getParentNode.get() match {
                        case _: ObjectCreationExpr => // AnonymousClass, ignore
                        case _ =>
                            val qualifiedSignature = resolvedMethod.getQualifiedSignature
                            val methodEntity = EntityManager.getMethodEntity(qualifiedSignature)
                            val typeEntity = methodEntity.getDeclareType
                            methodEntity.addExtendedMethods(
                                typeEntity.getExtendedTypes.asScala
                                    .flatMap(_.getHasMethods.asScala)
                                    .filter(_.getSignature == methodEntity.getSignature)
                                    .asJava
                            )
                    }
                } catch {
                    case e: Throwable =>
                        e.printStackTrace()
                }
            })
    }

    def buildProduceRelation(cus: Seq[CompilationUnit]): Unit = {
        cus.flatMap(_.findAll(classOf[MethodDeclaration]).asScala)
            .foreach(decl => {
                try {
                    val resolvedMethod = decl.resolve()
                    if (!resolvedMethod.declaringType().isInstanceOf[JavaParserAnonymousClassDeclaration]) {
                        val methodEntity = EntityManager.getMethodEntity(resolvedMethod.getQualifiedSignature)
                        resolvedMethod.getReturnType match {
                            case _ if resolvedMethod.getReturnType.isTypeVariable =>
                            case _ if resolvedMethod.getReturnType.isVoid =>
                            case _ if resolvedMethod.getReturnType.isPrimitive =>
                            case returnType if resolvedMethod.getReturnType.isReferenceType =>
                                val name = returnType.asReferenceType().getQualifiedName
                                methodEntity.setProduce(EntityManager.getTypeEntityOrCreate(name))
                            case returnType if resolvedMethod.getReturnType.isArray =>
                                val name = getComponentTypeRecursively(returnType)
                                if (name.isReferenceType) {
                                    methodEntity.setProduceMultiple(EntityManager.getTypeEntityOrCreate(name.asReferenceType().getQualifiedName))
                                }
                        }
                    }
                } catch {
                    case e: Throwable =>
                        e.printStackTrace()
                }
            })
        cus.flatMap(_.findAll(classOf[ConstructorDeclaration]).asScala)
            .foreach(decl => {
                try {
                    val resolvedMethod = decl.resolve()
                    if (!resolvedMethod.declaringType().isInstanceOf[JavaParserAnonymousClassDeclaration]) {
                        val methodEntity = EntityManager.getMethodEntity(resolvedMethod.getQualifiedSignature)
                        resolvedMethod.declaringType() match {
                            case returnType =>
                                val name = returnType.asReferenceType().getQualifiedName
                                methodEntity.setProduce(EntityManager.getTypeEntityOrCreate(name))
                        }
                    }
                } catch {
                    case e: Throwable =>
                        e.printStackTrace()
                }
            })
    }

    def main(args: Array[String]): Unit = {
        val strategy = new SymbolSolverCollectionStrategy()
        val projectPoi = strategy.collect(home / "lab" / "poi-4.0.0" / "src" / "java" path)

        log.info("Start parsing codes")
        val cus = projectPoi.getSourceRoots.asScala
            .flatMap(_.tryToParseParallelized().asScala)
            .map(_.getResult.get())

        val classOrInterfaceDeclarations = cus.flatMap(_.findAll(classOf[ClassOrInterfaceDeclaration]).asScala)
        val enumDeclarations = cus.flatMap(_.findAll(classOf[EnumDeclaration]).asScala)
        val typeDeclarations = classOrInterfaceDeclarations ++ enumDeclarations

        log.info("Start building type entities")
        buildTypeMapping(typeDeclarations)
        log.info("Start building extend relations")
        buildExtendRelation(classOrInterfaceDeclarations)
        log.info("Start building method extend relations")
        buildMethodExtendRelation(cus)
        log.info("Start building produce relations")
        buildProduceRelation(cus)

        val session = KnowledgeGraph.session
        EntityManager.save(session)
        KnowledgeGraph.close()
    }
}
