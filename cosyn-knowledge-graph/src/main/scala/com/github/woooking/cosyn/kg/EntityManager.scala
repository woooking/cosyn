package com.github.woooking.cosyn.kg

import com.github.javaparser.javadoc.Javadoc
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations._
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.woooking.cosyn.kg.entity.{EnumEntity, MethodEntity, MethodJavadocEntity, TypeEntity}
import org.neo4j.ogm.session.Session
import org.slf4s.Logging

import scala.collection.JavaConverters._
import scala.collection.concurrent
import scala.collection.parallel.mutable.ParSet
import scala.compat.java8.OptionConverters._

object EntityManager extends Logging {
    private val methodMapping = concurrent.TrieMap[String, MethodEntity]()
    private val typeMapping = concurrent.TrieMap[String, TypeEntity]()
    private val javadocs = ParSet[MethodJavadocEntity]()

    private val jdkSolver = new JavaParserTypeSolver(KnowledgeGraphConfig.global.jdkSrcCodeDir)

    private def createJavadocEntity(javadoc: Javadoc): MethodJavadocEntity = {
        val javadocEntity = new MethodJavadocEntity(javadoc)
        javadocs += javadocEntity
        javadocEntity
    }

    def createTypeEntity(resolved: ResolvedReferenceTypeDeclaration): Option[TypeEntity] = {
        try {
            val (typeEntity, _) = resolved match {
                case r: JavaParserClassDeclaration =>
                    (TypeEntity.fromDeclaration(r), r.getWrappedNode)
                case r: JavaParserInterfaceDeclaration =>
                    (TypeEntity.fromDeclaration(r), r.getWrappedNode)
                case r: JavaParserEnumDeclaration =>
                    (EnumEntity.fromDeclaration(r), r.getWrappedNode)
            }

            typeMapping(typeEntity.getQualifiedName) = typeEntity
            resolved.getDeclaredMethods.asScala.foreach(m => {
                try {
                    val astNode = m.asInstanceOf[JavaParserMethodDeclaration].getWrappedNode
                    val isDeprecated = astNode.getAnnotationByClass(classOf[Deprecated]).isPresent
                    val javadoc = astNode.getJavadoc.asScala.map(createJavadocEntity)
                    val methodEntity = new MethodEntity(m, isDeprecated, typeEntity, javadoc.orNull)
                    methodMapping(methodEntity.getQualifiedSignature) = methodEntity
                } catch {
                    case _: UnsolvedSymbolException =>
                    case _: UnsupportedOperationException =>
                }
            })

            resolved.getConstructors.asScala.foreach(m => {
                try {
                    val methodEntity = m match {
                        case d: JavaParserConstructorDeclaration[_] =>
                            val astNode = d.getWrappedNode
                            val javadoc = astNode.getJavadoc.asScala.map(createJavadocEntity)
                            val isDeprecated = d.getWrappedNode.getAnnotationByClass(classOf[Deprecated]).isPresent
                            new MethodEntity(m, isDeprecated, typeEntity, javadoc.orNull)
                        case _: DefaultConstructorDeclaration[_] =>
                            new MethodEntity(m, false, typeEntity, null)
                    }
                    methodMapping(methodEntity.getQualifiedSignature) = methodEntity
                } catch {
                    case _: UnsolvedSymbolException =>
                    case _: UnsupportedOperationException =>
                }
            })

            Some(typeEntity)
        } catch {
            case e: Throwable =>
                if (KnowledgeGraphConfig.global.debug) {
                    log.error("Error", e)
                }
                None
        }
    }

    def getTypeEntityOrCreate(qualifiedName: String): Option[TypeEntity] = {
        val result = createTypeEntity(jdkSolver.solveType(qualifiedName))
        result.foreach(
            typeMapping.getOrElseUpdate(qualifiedName, _)
        )
        result
    }

    def getMethodEntity(qualifiedSignature: String): MethodEntity = {
        methodMapping(qualifiedSignature)
    }

    def save(session: Session): Unit = {
        log.info(s"Javadoc Number: ${javadocs.size}")
        log.info(s"Method Number: ${methodMapping.size}")
        log.info(s"Type Number: ${typeMapping.size}")
        val tx = session.beginTransaction()
        try {
            val entities = typeMapping.values ++ methodMapping.values ++ javadocs
            session.save(entities.asJava, 1)
            tx.commit()
        } finally {
            tx.close()
        }
    }
}
