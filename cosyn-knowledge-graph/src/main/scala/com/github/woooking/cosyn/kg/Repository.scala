package com.github.woooking.cosyn.kg

import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.kg.entity.{EnumEntity, MethodEntity, TypeEntity}
import scala.collection.JavaConverters._
import org.neo4j.ogm.session.Session
import org.slf4s.Logging

import scala.reflect.ClassTag

trait Repository extends Logging {
    lazy val session: Session = KnowledgeGraph.session

    def get[T: ClassTag](id: String): Option[T] = {
        val clazz = implicitly[ClassTag[T]]
        Option(session.load(clazz.runtimeClass.asInstanceOf[Class[T]], id, 2))
    }

    def getMethod(qualifiedSignature: String): MethodEntity = profile("getMethodEntity") {
//        get[MethodEntity](qualifiedSignature).orNull
        KnowledgeGraph.methods.get(qualifiedSignature).orNull
    }

    //    def getType(qualifiedName: String): TypeEntity = profile("getTypeEntity") {
    //        get[EnumEntity](qualifiedName).getOrElse(
    //            get[TypeEntity](qualifiedName).getOrElse({
    //                log.warn(s"TypeEntity $qualifiedName not existed in graph!")
    //                null
    //            })
    //        )
    //    }

    def getType(qualifiedName: String): TypeEntity = profile("getTypeEntity") {
        KnowledgeGraph.enums.getOrElse(qualifiedName,
            KnowledgeGraph.types.getOrElse(qualifiedName, null)
        )
    }

    def getEnum(qualifiedName: String): EnumEntity = profile("getEnumEntity") {
        KnowledgeGraph.enums.get(qualifiedName).orNull
    }
}
