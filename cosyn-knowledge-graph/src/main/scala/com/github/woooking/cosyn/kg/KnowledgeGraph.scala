package com.github.woooking.cosyn.kg

import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.kg.entity._
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{Session, SessionFactory}

import scala.collection.JavaConverters._

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri(KnowledgeGraphConfig.global.uri)
        .credentials(KnowledgeGraphConfig.global.username, KnowledgeGraphConfig.global.password)
        .build()

    private val sessionFactory = new SessionFactory(configuration, KnowledgeGraphConfig.global.entityPackage)

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
            case e: Throwable =>
                e.printStackTrace()
                ???
        }
    }

    def close(): Unit = sessionFactory.close()
}
