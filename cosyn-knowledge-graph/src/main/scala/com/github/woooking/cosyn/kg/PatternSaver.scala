package com.github.woooking.cosyn.kg

import com.github.woooking.cosyn.comm.skeleton.Pattern
import com.github.woooking.cosyn.comm.skeleton.model.BlockStmt
import com.github.woooking.cosyn.comm.skeleton.visitors.{MethodCallCollector, TypeCollector}
import com.github.woooking.cosyn.comm.util.CodeUtil
import com.github.woooking.cosyn.kg.entity.{EnumEntity, MethodEntity, PatternEntity, TypeEntity}
import org.neo4j.ogm.session.Session
import io.circe.syntax._
import scala.collection.JavaConverters._

object PatternSaver {
    def savePatterns(patterns: Seq[Pattern]): Unit = {
        val session = KnowledgeGraph.session

        val tx = session.beginTransaction()
        try {
            session.save(patterns.map(toEntity(session, _)).asJava, 1)
            tx.commit()
        } finally {
            tx.close()
        }
        KnowledgeGraph.close()
    }

    private def toEntity(session: Session, pattern: Pattern): PatternEntity = {
        val entity = PatternEntity.create(pattern.asJson.noSpaces)

        val types = TypeCollector.instance[BlockStmt].collect(pattern.stmts)
            .map(CodeUtil.coreType)
            .map(loadTypeOrEnumEntity(session, _))
            .filter(_ != null)
        entity.addHasTypes(types.asJavaCollection)

        val methods = MethodCallCollector.instance[BlockStmt].collect(pattern.stmts)
            .map(session.load(classOf[MethodEntity], _))
            .filter(_ != null)
        entity.addHasMethods(methods.asJavaCollection)

        entity
    }

    private def loadTypeOrEnumEntity(session: Session, qualifiedName: String): TypeEntity = {
        session.load(classOf[EnumEntity], qualifiedName) match {
            case null => session.load(classOf[TypeEntity], qualifiedName)
            case entity => entity
        }
    }
}
