package com.github.woooking.cosyn.pattern

import com.github.javaparser.ast.Modifier
import com.github.woooking.cosyn.kg.entity.{MethodEntity, TypeEntity}
import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, Type}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{Session, SessionFactory}

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.collection.JavaConverters._

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri("bolt://162.105.88.181")
        .credentials("neo4j", "poi")
        .build()

    val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.kg.entity")

    val session: Session = sessionFactory.openSession()

    private def getIterablePaths(qualifiedName: String, path: List[TypeEntity]): Set[List[TypeEntity]] = {
        val entity = getTypeEntity(qualifiedName)
        val newPath = entity :: path
        (Set(newPath) /: entity.getIterables.asScala) ((s, t) => s ++ getIterablePaths(t.getQualifiedName, newPath))
    }

    def getIterablePaths(basicType: BasicType): Set[List[TypeEntity]] = {
        getIterablePaths(basicType.ty, Nil)
    }

    def getMethodEntity(qualifiedSignature: String): MethodEntity = {
        session.load(classOf[MethodEntity], qualifiedSignature)
    }

    def getTypeEntity(qualifiedName: String): TypeEntity = {
        session.load(classOf[TypeEntity], qualifiedName)
    }

    @tailrec
    private def getMethodProto(method: MethodEntity): String = {
        val entity = session.load(classOf[MethodEntity], method.getQualifiedSignature)
        entity.getExtendedMethods.asScala.headOption match {
            case Some(extended) => getMethodProto(extended)
            case None => entity.getQualifiedSignature
        }
    }

    def getMethodProto(method: String): String = {
        val entity = session.load(classOf[MethodEntity], method)
        if (entity == null) method
        else getMethodProto(entity)
    }

    def close(): Unit = {
        sessionFactory.close()
    }
}
