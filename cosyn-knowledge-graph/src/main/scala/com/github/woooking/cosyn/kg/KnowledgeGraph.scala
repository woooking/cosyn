package com.github.woooking.cosyn.kg

import com.github.woooking.cosyn.comm.config.Neo4jConfig
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.{Session, SessionFactory}

object KnowledgeGraph {
    private val configuration = new Configuration.Builder()
        .uri(Neo4jConfig.uri)
        .credentials(Neo4jConfig.username, Neo4jConfig.password)
        .build()

    private val sessionFactory = new SessionFactory(configuration, Neo4jConfig.entityPackage)

    val session: Session = sessionFactory.openSession()

    def close(): Unit = sessionFactory.close()
}
