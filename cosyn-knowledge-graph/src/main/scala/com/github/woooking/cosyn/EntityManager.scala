package com.github.woooking.cosyn

import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations._
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.woooking.cosyn.entity.{EnumEntity, MethodEntity, TypeEntity}
import better.files.File.home
import org.neo4j.ogm.session.Session
import org.slf4s.Logging

import scala.collection.mutable
import scala.collection.JavaConverters._

object EntityManager extends Logging {
    private val methodMapping = mutable.Map[String, MethodEntity]()
    private val typeMapping = mutable.Map[String, TypeEntity]()

    private val jdkSolver = new JavaParserTypeSolver(home / "lab" / "jdk-11" / "src" path)

    def createTypeEntity(resolved: ResolvedReferenceTypeDeclaration): TypeEntity = {
        val (typeEntity, decl) = resolved match {
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
                val methodEntity = new MethodEntity(m, isDeprecated, typeEntity, astNode.getJavadocComment.orElse(null))
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
                        val isDeprecated = d.getWrappedNode.getAnnotationByClass(classOf[Deprecated]).isPresent
                        new MethodEntity(m, isDeprecated, typeEntity, d.getWrappedNode.getJavadocComment.orElse(null))
                    case d: DefaultConstructorDeclaration[_] =>
                        new MethodEntity(m, false, typeEntity, null)
                }
                methodMapping(methodEntity.getQualifiedSignature) = methodEntity
            } catch {
                case _: UnsolvedSymbolException =>
                case _: UnsupportedOperationException =>
            }
        })

        //        decl.getMethods.asScala.foreach(m => {
        //            try {
        //                val methodEntity = new MethodEntity(m.resolve(), typeEntity, m.getJavadocComment.orElse(null))
        //                methodMapping(methodEntity.getQualifiedSignature) = methodEntity
        //            } catch {
        //                case _: UnsolvedSymbolException =>
        //                case _: UnsupportedOperationException =>
        //            }
        //        })
        //        decl.getConstructors.asScala.foreach(m => {
        //            try {
        //                val methodEntity = new MethodEntity(m.resolve(), typeEntity, m.getJavadocComment.orElse(null))
        //                methodMapping(methodEntity.getQualifiedSignature) = methodEntity
        //            } catch {
        //                case _: UnsolvedSymbolException =>
        //                case _: UnsupportedOperationException =>
        //            }
        //        })
        typeEntity
    }

    def getTypeEntityOrCreate(qualifiedName: String): TypeEntity = {
        typeMapping.getOrElseUpdate(qualifiedName, createTypeEntity(jdkSolver.solveType(qualifiedName)))
    }

    def getMethodEntity(qualifiedSignature: String): MethodEntity = {
        methodMapping(qualifiedSignature)
    }

    def save(session: Session): Unit = {
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
    }
}
