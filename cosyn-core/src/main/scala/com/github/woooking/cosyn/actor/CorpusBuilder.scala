package com.github.woooking.cosyn.actor

import akka.actor.{ActorSystem, FSM, Props, Terminated}
import com.github.woooking.cosyn.actor.CorpusBuilder.{Data, State}
import com.github.woooking.cosyn.actor.ProjectBuilder.{NoTask, Task}
import com.github.woooking.cosyn.model.ProjectInfo

class CorpusBuilder extends FSM[State, Data] {

    import CorpusBuilder._

    startWith(Initing, Uninitialized)

    when(Initing) {
        case Event(Init(actorNum, files), Uninitialized) =>
            (0 until actorNum).foreach(i => {
                val actorRef = context.actorOf(ProjectBuilder.props(self), s"project-builder-$i")
                context.watch(actorRef)
            })
            goto(Ready) using Projects(files, actorNum)
    }

    when(Ready) {
        case Event(AskForTask, Projects(Nil, _)) =>
            sender() ! NoTask
            stay()
        case Event(AskForTask, Projects(f :: fs, aliveActorNum)) =>
            sender() ! Task(f)
            stay() using Projects(fs, aliveActorNum)
        case Event(Terminated(_), Projects(files, aliveActorNum)) =>
            if (aliveActorNum == 1) stop(FSM.Normal)
            else stay() using Projects(files, aliveActorNum - 1)
    }

    onTermination {
        case StopEvent(FSM.Normal, _, _) =>
            println(s"${self.path.name}: successfully stopped.")
            context stop self
            context.system.terminate()
    }

    initialize()
}

object CorpusBuilder {

    final case class Init(actorNum: Int, projects: List[ProjectInfo])

    final case object AskForTask

    sealed trait State

    case object Initing extends State

    case object Ready extends State

    sealed trait Data

    case object Uninitialized extends Data

    final case class Projects(projects: List[ProjectInfo], aliveActorNum: Int) extends Data

    private val system = ActorSystem("cosyn")
    private val corpusBuilder = system.actorOf(Props[CorpusBuilder])

    def main(args: Array[String]): Unit = {
        corpusBuilder ! Init(8, ProjectInfo.projects)
    }
}
