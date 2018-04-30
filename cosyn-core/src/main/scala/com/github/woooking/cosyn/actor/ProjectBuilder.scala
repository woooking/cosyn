package com.github.woooking.cosyn.actor

import akka.actor.{ActorRef, FSM, Props}
import com.github.woooking.cosyn.actor.CorpusBuilder.AskForTask
import com.github.woooking.cosyn.actor.ProjectBuilder.{Data, State}
import com.github.woooking.cosyn.model.ProjectInfo

class ProjectBuilder(parent: ActorRef) extends FSM[State, Data] {

    import ProjectBuilder._

    startWith(Waiting, Dummy)

    when(Waiting) {
        case Event(Task(file), Dummy) =>
            new Thread(new ProjectProcessor(file, self)).run()
            goto(Processing) using Project(file)
        case Event(NoTask, Dummy) =>
            stop(FSM.Normal)
    }

    when(Processing) {
        case Event(TaskFinished, _) =>
            goto(Waiting) using Dummy
    }

    onTransition {
        case _ -> Waiting => parent ! AskForTask
    }

    onTermination {
        case StopEvent(FSM.Normal, _, _) =>
            println(s"${self.path.name}: successfully stopped.")
    }

    initialize()
}

object ProjectBuilder {

    final case object NoTask

    final case object TaskFinished

    final case class Task(project: ProjectInfo)

    sealed trait State

    case object Waiting extends State

    case object Processing extends State

    sealed trait Data

    case object Dummy extends Data

    final case class Project(project: ProjectInfo) extends Data

    def props(parent: ActorRef): Props = Props(new ProjectBuilder(parent))
}
