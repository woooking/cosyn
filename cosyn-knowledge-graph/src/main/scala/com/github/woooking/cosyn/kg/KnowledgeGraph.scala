package com.github.woooking.cosyn.kg

import com.github.woooking.cosyn.comm.config.Neo4jConfig
import com.github.woooking.cosyn.kg.entity._
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{LoadStrategy, Neo4jSession, Session, SessionFactory}
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import org.neo4j.ogm.driver.Driver
import org.neo4j.ogm.exception.core.ConfigurationException

import scala.collection.JavaConverters._

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri(Neo4jConfig.uri)
        .credentials(Neo4jConfig.username, Neo4jConfig.password)
        .build()

    private val sessionFactory = new SessionFactory(configuration, Neo4jConfig.entityPackage)

    val session: Session = sessionFactory.openSession()

    val (types, enums, methods) = profile("load-all") {
        try {

            val types = session.loadAll(classOf[TypeEntity]).asScala.map(e => e.getQualifiedName -> e).toSeq
            val enums = session.loadAll(classOf[EnumEntity]).asScala.map(e => e.getQualifiedName -> e).toSeq
            val methods = session.loadAll(classOf[MethodEntity]).asScala.map(e => e.getQualifiedSignature -> e).toSeq
            session.loadAll(classOf[MethodJavadocEntity])
            session.loadAll(classOf[MethodParamJavadocEntity])
            session.loadAll(classOf[PatternEntity])
            (Map(types: _*), Map(enums: _*), Map(methods: _*))
        } catch {
            case e =>
                e.printStackTrace()
                ???
        }
    }

    def close(): Unit = sessionFactory.close()
}
