package com.github.woooking.cosyn.actor

import akka.actor.{ActorRef, FSM, Props}
import better.files.File
import com.github.woooking.cosyn.actor.CorpusMiner.AskForTask
import com.github.woooking.cosyn.actor.GraphFileReader.{Data, State}
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGNode, SimpleDFG}
import com.github.woooking.cosyn.model.ProjectInfo
import spray.json._
import com.github.woooking.cosyn.CosynJsonProtocol._
import com.github.woooking.cosyn.cosyn.filter.{CombinedFilter, DFGNodeFilter}

class GraphFileReader(parent: ActorRef, nodes: Seq[DFGNode]) extends FSM[State, Data] {

    import GraphFileReader._

    val filter = CombinedFilter(nodes.map(DFGNodeFilter))

    startWith(DummyState, DummyData)

    when(DummyState) {
        case Event(Task(file), DummyData) =>
            val dfgs = filter.filter(file.contentAsString.parseJson.convertTo[Seq[SimpleDFG]])
            parent ! AskForTask(dfgs)
            stay()
        case Event(NoTask, DummyData) =>
            stop(FSM.Normal)
    }

    onTransition {
        case _ -> DummyState => parent ! AskForTask(Seq.empty)
    }

    onTermination {
        case StopEvent(FSM.Normal, _, _) =>
            println(s"${self.path.name}: successfully stopped.")
    }

    initialize()
}

object GraphFileReader {

    final case object NoTask

    final case object TaskFinished

    final case class Task(graphFile: File)

    sealed trait State

    case object DummyState extends State

    sealed trait Data

    case object DummyData extends Data

    final case class Project(project: ProjectInfo) extends Data

    def props(parent: ActorRef, nodes: Seq[DFGNode]): Props = Props(new GraphFileReader(parent, nodes))
}
