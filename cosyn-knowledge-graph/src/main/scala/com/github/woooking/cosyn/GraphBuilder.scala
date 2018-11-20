package com.github.woooking.cosyn

import better.files.File.home
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.`type`.ClassOrInterfaceType
import com.github.javaparser.ast.body.{ClassOrInterfaceDeclaration, MethodDeclaration}
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.resolution.types.ResolvedReferenceType
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.entity.{MethodEntity, TypeEntity}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

object GraphBuilder {
    private val methodMapping = mutable.Map[String, MethodEntity]()
    private val typeMapping = mutable.Map[String, TypeEntity]()

    private def type2entity(decl: ClassOrInterfaceType) = {
        val qualifiedName = decl.resolve().getQualifiedName
        typeMapping(qualifiedName)
    }

    private def type2entity(decl: ResolvedReferenceTypeDeclaration) = {
        val qualifiedName = decl.getQualifiedName
        typeMapping(qualifiedName)
    }

    def buildTypeMapping(cus: Seq[CompilationUnit]) = {
        cus.flatMap(_.findAll(classOf[ClassOrInterfaceDeclaration]).asScala)
            .foreach(decl => {
                val qualifiedName = decl.resolve().getQualifiedName
                val typeEntity = new TypeEntity(qualifiedName, decl.isInterface)
                typeMapping(qualifiedName) = typeEntity
                decl.getMethods.asScala.foreach(m => {
                    try {
                        val qualifiedSignature = m.resolve().getQualifiedSignature
                        methodMapping(qualifiedSignature) = new MethodEntity(qualifiedSignature)
                    } catch {
                        case _: UnsolvedSymbolException =>
                    }
                })
            })
    }

    def buildExtendRelation(cus: Seq[CompilationUnit]) = {
        cus.flatMap(_.findAll(classOf[ClassOrInterfaceDeclaration]).asScala)
            .foreach(decl => {
                val qualifiedName = decl.resolve().getQualifiedName
                val typeEntity = typeMapping(qualifiedName)
                typeEntity.addExtendedTypes(decl.getExtendedTypes.asScala.map(type2entity).toSet.asJava)
                typeEntity.addImplementedTypes(decl.getImplementedTypes.asScala.map(type2entity).toSet.asJava)
            })
    }

    def buildMethodImplementRelation(cus: Seq[CompilationUnit]) = {
        cus.flatMap(_.findAll(classOf[MethodDeclaration]).asScala)
            .foreach(decl => {
                val resolvedMethod = decl.resolve()
                val qualifiedSignature = resolvedMethod.getQualifiedSignature
                val methodEntity = methodMapping(qualifiedSignature)
                val typeEntity = type2entity(resolvedMethod.declaringType())
            })
    }

    def main(args: Array[String]): Unit = {
        val projectRoot = new SymbolSolverCollectionStrategy().collect(home / "lab" / "poi-3.14" / "src" / "java" path)
        val cus = projectRoot.getSourceRoots.asScala
            .flatMap(_.tryToParseParallelized().asScala)
            .filter(_.isSuccessful)
            .map(_.getResult.get())

        buildTypeMapping(cus)

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
            typeMapping.foreach(p => session.save(p._2))
            methodMapping.foreach(p => session.save(p._2))
            tx.commit()
        } finally {
            tx.close()
        }
        sessionFactory.close()
    }
}
