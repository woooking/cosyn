package com.github.woooking.cosyn

import com.github.woooking.cosyn.entity.{EnumEntity, MethodEntity, PatternEntity, TypeEntity}
import com.github.woooking.cosyn.skeleton.Pattern
import com.github.woooking.cosyn.skeleton.model.BlockStmt
import com.github.woooking.cosyn.skeleton.visitors.{MethodCallCollector, TypeCollector}
import com.github.woooking.cosyn.util.CodeUtil
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{write, writePretty}
import org.json4s.{Formats, NoTypeHints}
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{Session, SessionFactory}

import scala.collection.JavaConverters._

object PatternSaver {
    private implicit val formats: Formats = Serialization.formats(NoTypeHints)

    def savePatterns(patterns: Seq[Pattern]): Unit = {
        val configuration = new Configuration.Builder()
            .uri("bolt://localhost")
            .credentials("neo4j", "poi")
            .build()

        val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.entity")
        val session = sessionFactory.openSession()

        val tx = session.beginTransaction()
        try {
            session.save(patterns.map(toEntity(session, _)).asJava, 1)
            tx.commit()
        } finally {
            tx.close()
        }
        sessionFactory.close()
    }

    private def toEntity(session: Session, pattern: Pattern): PatternEntity = {
        println(writePretty(pattern))
        val entity = PatternEntity.create(write(pattern))

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
