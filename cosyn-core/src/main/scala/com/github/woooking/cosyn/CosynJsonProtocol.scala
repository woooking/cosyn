package com.github.woooking.cosyn

import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGEdge, DFGNode, SimpleDFG}
import com.github.woooking.cosyn.util.GraphUtil
import de.parsemis.graph.Edge
import spray.json.{DefaultJsonProtocol, DeserializationException, JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

import scala.collection.JavaConverters._

object CosynJsonProtocol extends DefaultJsonProtocol {

    implicit object DFGNodeJsonFormat extends RootJsonFormat[DFGNode] {
        def write(node: DFGNode) = {
            JsObject("op" -> JsString(node.op.toString), "info" -> JsString(node.info))
        }

        def read(value: JsValue) =
            value.asJsObject.getFields("op", "info") match {
                case Seq(JsString(op), JsString(info)) => DFGNode(DFGNode.NodeType.withName(op), info)
                case _ => throw DeserializationException("")
            }
    }

    implicit object SimpleDFGJsonFormat extends RootJsonFormat[SimpleDFG] {
        def write(c: SimpleDFG) = {
            val nodes = JsArray(c.nodeIterator().asScala.toVector.map(_.getLabel).map(DFGNodeJsonFormat.write))
            val edges = JsArray(
                c.edgeIterator().asScala.toVector.map(e => JsObject(
                    "from" -> JsNumber(GraphUtil.fromNode(e).getIndex),
                    "to" -> JsNumber(GraphUtil.toNode(e).getIndex)
                ))
            )
            JsObject("nodes" -> nodes, "edges" -> edges)
        }

        def read(value: JsValue) = value.asJsObject().getFields("nodes", "edges") match {
            case Seq(JsArray(nodes), JsArray(edges)) =>
                val dfg = new SimpleDFG(null)
                val dNodes = nodes.map(_.convertTo[DFGNode]).map(dfg.addNode)
                edges
                    .map(edge => edge.asJsObject.fields("from").convertTo[Int] -> edge.asJsObject.fields("to").convertTo[Int])
                    .foreach {
                        case (from, to) => dfg.addEdge(dNodes(from), dNodes(to), DFGEdge.singleton, Edge.OUTGOING)
                    }
                dfg
            case _ => throw DeserializationException("")
        }
    }

}
