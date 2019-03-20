package com.github.woooking.cosyn.kg.repository.impl

import com.github.javaparser.ast.Modifier
import com.github.woooking.cosyn.comm.skeleton.model.{ArrayType, BasicType, Type}
import com.github.woooking.cosyn.comm.util.TimeUtil.profile
import com.github.woooking.cosyn.kg.entity.{MethodEntity, TypeEntity}
import com.github.woooking.cosyn.kg.repository.MethodEntityRepository

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.collection.immutable.Queue
import scala.collection.mutable

class MethodEntityRepositoryImpl extends MethodEntityRepository {
    private val producersCache = mutable.Map[Type, Set[MethodEntity]]()

    def get(qualifiedSignature: String): MethodEntity = profile("getMethodEntity") {
        session.load(classOf[MethodEntity], qualifiedSignature)
    }
    private def isAccessible(entity: MethodEntity): Boolean = {
        val methodEntity = session.load(classOf[MethodEntity], entity.getQualifiedSignature)
        // TODO: 考虑继承和protected
        methodEntity.getAccessSpecifier == Modifier.Keyword.PUBLIC ||
            methodEntity.getDeclareType.isInterface && methodEntity.getAccessSpecifier == Modifier.Keyword.PACKAGE_PRIVATE
    }

    private def producers(entity: TypeEntity, multiple: Boolean): Set[MethodEntity] = {
        case class ProducerContext(deleteMap: Map[MethodEntity, List[MethodEntity]], visited: Set[MethodEntity], result: Set[MethodEntity])

        object ProducerContext {
            def init = ProducerContext(Map(), Set(), Set())
        }

        def processMethod(entity: MethodEntity, producerContext: ProducerContext): ProducerContext = {
            val methodEntity = session.load(classOf[MethodEntity], entity.getQualifiedSignature)
            val ProducerContext(deleteMap, visited, result) = producerContext
            val extendedMethods = methodEntity.getExtendedMethods.asScala.filter(isAccessible)
            if (extendedMethods.exists(visited.contains)) ProducerContext(deleteMap, visited + methodEntity, result)
            else {
                val newDeleteMap = (deleteMap /: extendedMethods) ((map, extendedMethod) => map.updated(extendedMethod, methodEntity :: map.getOrElse(extendedMethod, Nil)))
                ProducerContext(newDeleteMap - methodEntity, visited + methodEntity, result + methodEntity -- deleteMap.getOrElse(methodEntity, Nil))
            }
        }

        @tailrec
        def process(queue: Queue[TypeEntity], processedType: Set[TypeEntity], producerContext: ProducerContext): Set[MethodEntity] = {
            queue.dequeueOption match {
                case Some((front, rest)) if processedType.contains(front) =>
                    process(rest, processedType, producerContext)
                case Some((front, rest)) =>
                    val frontEntity = session.load(classOf[TypeEntity], front.getQualifiedName)
                    val methods = (if (multiple) frontEntity.getMultipleProducers else frontEntity.getProducers).asScala.filter(isAccessible)
                    val newProducerContext = (producerContext /: methods) {
                        case (c, m) => processMethod(m, c)
                    }
                    process(rest ++ frontEntity.getExtendedTypes.asScala, processedType + frontEntity, newProducerContext)
                case None =>
                    producerContext.result
            }
        }

        process(Queue(entity), Set(), ProducerContext.init)
    }

    def producers(ty: Type): Set[MethodEntity] = profile("producers") {
        producersCache.get(ty) match {
            case Some(value) => value
            case None =>
                val methods = ty match {
                    case BasicType(t) =>
                        val typeEntity = session.load(classOf[TypeEntity], t)
                        producers(typeEntity, multiple = false)
                            .map(m => session.load(classOf[MethodEntity], m.getQualifiedSignature))
                            .filter(!_.isDeprecated)
                    case ArrayType(BasicType(t)) =>
                        val typeEntity = session.load(classOf[TypeEntity], t)
                        producers(typeEntity, multiple = true)
                            .map(m => session.load(classOf[MethodEntity], m.getQualifiedSignature))
                            .filter(!_.isDeprecated)
                    case _ =>
                        ???
                }
                producersCache(ty) = methods
                methods
        }
    }

    @tailrec
    private def getMethodProtoRec(method: MethodEntity): String = {
        val entity = get(method.getQualifiedSignature)
        entity.getExtendedMethods.asScala.headOption match {
            case Some(extended) => getMethodProtoRec(extended)
            case None => entity.getQualifiedSignature
        }
    }

    def getMethodProto(method: String): String = {
        val entity = session.load(classOf[MethodEntity], method)
        if (entity == null) method
        else getMethodProtoRec(entity)
    }
}
