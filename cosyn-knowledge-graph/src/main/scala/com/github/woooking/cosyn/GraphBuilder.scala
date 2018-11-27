package com.github.woooking.cosyn

import better.files.File.home
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, ConstructorDeclaration, EnumDeclaration, MethodDeclaration}
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserAnonymousClassDeclaration
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.entity.{EnumEntity, MethodEntity, TypeEntity}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

object GraphBuilder {
    private val methodMapping = mutable.Map[String, MethodEntity]()
    private val typeMapping = mutable.Map[String, TypeEntity]()

    private def type2entity(decl: ClassOrInterfaceType) = {
        Try {
            val qualifiedName = decl.resolve().getQualifiedName
            typeMapping(qualifiedName)
        }.toOption.toSeq
    }

    def buildTypeMapping(cus: Seq[CompilationUnit]): Unit = {
        cus.flatMap(_.findAll(classOf[ClassOrInterfaceDeclaration]).asScala)
            .foreach(decl => {
                val typeEntity = new TypeEntity(decl.resolve(), decl.isInterface)
                typeMapping(typeEntity.getQualifiedName) = typeEntity
                decl.getMethods.asScala.foreach(m => {
                    try {
                        val methodEntity = new MethodEntity(m.resolve(), typeEntity)
                        methodMapping(methodEntity.getQualifiedSignature) = methodEntity
                    } catch {
                        case _: UnsolvedSymbolException =>
                    }
                })
                decl.getConstructors.asScala.foreach(m => {
                    try {
                        val methodEntity = new MethodEntity(m.resolve(), typeEntity)
                        methodMapping(methodEntity.getQualifiedSignature) = methodEntity
                    } catch {
                        case _: UnsolvedSymbolException =>
                    }
                })
            })
        cus.flatMap(_.findAll(classOf[EnumDeclaration]).asScala)
            .foreach(decl => {
                val typeEntity = new EnumEntity(decl.resolve())
                typeMapping(typeEntity.getQualifiedName) = typeEntity
                decl.getMethods.asScala.foreach(m => {
                    try {
                        val methodEntity = new MethodEntity(m.resolve(), typeEntity)
                        methodMapping(methodEntity.getQualifiedSignature) = methodEntity
                    } catch {
                        case _: UnsolvedSymbolException =>
                        case _: UnsupportedOperationException =>
                    }
                })
                decl.getConstructors.asScala.foreach(m => {
                    try {
                        val methodEntity = new MethodEntity(m.resolve(), typeEntity)
                        methodMapping(methodEntity.getQualifiedSignature) = methodEntity
                    } catch {
                        case _: UnsolvedSymbolException =>
                    }
                })
            })
    }

    def buildExtendRelation(cus: Seq[CompilationUnit]): Unit = {
        cus.flatMap(_.findAll(classOf[ClassOrInterfaceDeclaration]).asScala)
            .foreach(decl => {
                val qualifiedName = decl.resolve().getQualifiedName
                val typeEntity = typeMapping(qualifiedName)
                typeEntity.addExtendedTypes((decl.getExtendedTypes.asScala ++ decl.getImplementedTypes.asScala).flatMap(type2entity).toSet.asJava)
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
        val projectRoot = new SymbolSolverCollectionStrategy().collect(home / "lab" / "poi-4.0.0" / "src" / "java" path)
        val cus = projectRoot.getSourceRoots.asScala
            .flatMap(_.tryToParseParallelized().asScala)
            .filter(_.isSuccessful)
            .map(_.getResult.get())

        buildTypeMapping(cus)
        buildExtendRelation(cus)
        buildMethodExtendRelation(cus)
        buildProduceRelation(cus)

        val configuration = new Configuration.Builder()
            .uri("bolt://localhost")
            .credentials("neo4j", "poi")
            .build()

        val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.entity")
        val session = sessionFactory.openSession()
        println(s"Method Number: ${methodMapping.size}")
        println(s"Type Number: ${typeMapping.size}")
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
