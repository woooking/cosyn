package com.github.woooking.cosyn.knowledge_graph

import com.github.woooking.cosyn.entity.TypeEntity
import com.google.common.collect.ImmutableMap
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory
import scala.collection.JavaConverters._

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri("bolt://localhost")
        .credentials("neo4j", "poi")
        .build()

    val sessionFactory = new SessionFactory(configuration, "com.github.woooking.cosyn.entity")

    private def isAssignable(source: TypeEntity, target: TypeEntity): Boolean = {
        if (source == target) true
        else source.getExtendedTypes.asScala.exists(e => isAssignable(e, target))
    }

    def isAssignable(source: String, target: String): Boolean = {
        val session = sessionFactory.openSession()
        val sourceEntity = session.load(classOf[TypeEntity], source)
        val targetEntity = session.load(classOf[TypeEntity], target)
        if (sourceEntity == null || targetEntity == null) return false
        isAssignable(sourceEntity, targetEntity)
    }
}
