package com.github.woooking.cosyn

import better.files.File.home
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body._
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.resolution.types.ResolvedType
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.{JavaParserAnonymousClassDeclaration, JavaParserClassDeclaration, JavaParserEnumDeclaration, JavaParserInterfaceDeclaration}
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.entity.{EnumEntity, MethodEntity, TypeEntity}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory
import org.slf4s.Logging

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.mutable

object GraphBuilder extends Logging {
    private val methodMapping = mutable.Map[String, MethodEntity]()
    private val typeMapping = mutable.Map[String, TypeEntity]()

    private val jdkSolver = new JavaParserTypeSolver(home / "lab" / "jdk-11" / "src" path)

    @tailrec
    private def getComponentTypeRecursively(ty: ResolvedType): ResolvedType = {
        ty match {
            case _ if ty.isArray => getComponentTypeRecursively(ty.asArrayType().getComponentType)
            case _ => ty
        }
    }

    private def buildTypeEntity(resolved: ResolvedReferenceTypeDeclaration): TypeEntity = {
        val (typeEntity, decl) = resolved match {
            case r: JavaParserClassDeclaration =>
                (TypeEntity.fromDeclaration(r.getWrappedNode), r.getWrappedNode)
            case r: JavaParserInterfaceDeclaration =>
                (TypeEntity.fromDeclaration(r.getWrappedNode), r.getWrappedNode)
            case r: JavaParserEnumDeclaration =>
                (EnumEntity.fromDeclaration(r.getWrappedNode), r.getWrappedNode)
        }

        typeMapping(typeEntity.getQualifiedName) = typeEntity
        decl.getMethods.asScala.foreach(m => {
            try {
                val methodEntity = new MethodEntity(m.resolve(), typeEntity, m.getJavadocComment.orElse(null))
                methodMapping(methodEntity.getQualifiedSignature) = methodEntity
            } catch {
                case _: UnsolvedSymbolException =>
                case _: UnsupportedOperationException =>
            }
        })
        decl.getConstructors.asScala.foreach(m => {
            try {
                val methodEntity = new MethodEntity(m.resolve(), typeEntity, m.getJavadocComment.orElse(null))
                methodMapping(methodEntity.getQualifiedSignature) = methodEntity
            } catch {
                case _: UnsolvedSymbolException =>
                case _: UnsupportedOperationException =>
            }
        })
        typeEntity
    }

    private def type2entity(decl: ClassOrInterfaceType) = {
        val qualifiedName = decl.resolve().getQualifiedName
        typeMapping.getOrElseUpdate(qualifiedName, buildTypeEntity(jdkSolver.solveType(qualifiedName)))
    }

    private def getIterableType(resolved: ResolvedReferenceTypeDeclaration): TypeEntity = resolved.getAllAncestors.asScala
        .find(_.getQualifiedName == "java.lang.Iterable")
        .map(_.getGenericParameterByName("T").get())
        .filter(_.isReferenceType)
        .map(_.asReferenceType().getQualifiedName)
        .map(typeMapping.apply).orNull

    def buildTypeMapping(typeDeclarations: Seq[TypeDeclaration[_]]): Unit = {
        typeDeclarations
            .map(_.resolve())
            .foreach(buildTypeEntity)
    }

    def buildExtendRelation(classDecls: Seq[ClassOrInterfaceDeclaration]): Unit = {
        classDecls
            .foreach(decl => {
                val qualifiedName = decl.resolve().getQualifiedName
                val typeEntity = typeMapping(qualifiedName)
                val parentTypes = decl.getExtendedTypes.asScala ++ decl.getImplementedTypes.asScala
                typeEntity.addExtendedTypes(parentTypes.map(type2entity).toSet.asJava)
                typeEntity.setIterableType(getIterableType(decl.resolve()))
            })
    }

    def buildMethodExtendRelation(cus: Seq[CompilationUnit]): Unit = {
        cus.flatMap(_.findAll(classOf[MethodDeclaration]).asScala)
            .foreach(decl => {
                try {
                    val resolvedMethod = decl.resolve()
                    if (!resolvedMethod.declaringType().isInstanceOf[JavaParserAnonymousClassDeclaration]) {
                        val qualifiedSignature = resolvedMethod.getQualifiedSignature
                        val methodEntity = methodMapping(qualifiedSignature)
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
                        val methodEntity = methodMapping(resolvedMethod.getQualifiedSignature)
                        resolvedMethod.getReturnType match {
                            case returnType if resolvedMethod.getReturnType.isPrimitive =>
                            case returnType if resolvedMethod.getReturnType.isReferenceType =>
                                val name = returnType.asReferenceType().getQualifiedName
                                methodEntity.setProduce(typeMapping(name))
                            case returnType if resolvedMethod.getReturnType.isArray =>
                                val name = getComponentTypeRecursively(returnType)
                                methodEntity.setProduce(typeMapping(name.asReferenceType().getQualifiedName))
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
                        val methodEntity = methodMapping(resolvedMethod.getQualifiedSignature)
                        resolvedMethod.declaringType() match {
                            case returnType =>
                                val name = returnType.asReferenceType().getQualifiedName
                                methodEntity.setProduce(typeMapping(name))
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

        log.info("Start saving entities")
        val configuration = new Configuration.Builder()
            .uri("bolt://localhost")
            .credentials("neo4j", "poi")
            .build()

        val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.entity")
        val session = sessionFactory.openSession()
        log.info(s"Method Number: ${methodMapping.size}")
        log.info(s"Type Number: ${typeMapping.size}")
        val tx = session.beginTransaction()
        try {
            val entities = typeMapping.values ++ methodMapping.values
            session.save(entities.asJava, 1)
            tx.commit()
        } finally {
            tx.close()
        }
        sessionFactory.close()
    }
}
