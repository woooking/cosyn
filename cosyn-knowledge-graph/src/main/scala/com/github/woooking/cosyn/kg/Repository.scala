package com.github.woooking.cosyn.kg

import org.neo4j.ogm.session.Session

trait Repository {
    lazy val session: Session = KnowledgeGraph.session
}
