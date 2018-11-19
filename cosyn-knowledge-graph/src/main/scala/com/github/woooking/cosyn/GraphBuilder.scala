package com.github.woooking.cosyn

import better.files.File.home
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy
import com.github.woooking.cosyn.entity.{MethodEntity, TypeEntity}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

object GraphBuilder {
    private val methodMapping = mutable.Map[String, MethodEntity]()
    private val typeMapping = mutable.Map[String, TypeEntity]()

    def main(args: Array[String]): Unit = {
        val projectRoot = new SymbolSolverCollectionStrategy().collect(home / "lab" / "poi-3.14" / "src" / "java" path)
        val cus = projectRoot.getSourceRoots.asScala
            .flatMap(_.tryToParseParallelized().asScala)
            .filter(_.isSuccessful)
            .map(_.getResult.get())

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
