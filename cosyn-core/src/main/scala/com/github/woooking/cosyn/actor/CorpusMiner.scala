package com.github.woooking.cosyn.actor

import akka.actor.{ActorSystem, FSM, Props, Terminated}
import better.files._
import com.github.woooking.cosyn.actor.CorpusMiner.{Data, State}
import com.github.woooking.cosyn.actor.GraphFileReader.{NoTask, Task}
import com.github.woooking.cosyn.cosyn.filter.{CombinedFilter, FragmentFilter}
import com.github.woooking.cosyn.dfgprocessor.dfg.DFGNode.NodeType
import com.github.woooking.cosyn.dfgprocessor.dfg.{DFGNode, SimpleDFG}
import com.github.woooking.cosyn.mine.Miner
import com.github.woooking.cosyn.model.ProjectInfo
import com.github.woooking.cosyn.util.GraphUtil
import de.parsemis.miner.general.IntFrequency

class CorpusMiner(nodes: Seq[DFGNode], limit: Int = Int.MaxValue) extends FSM[State, Data] {
    import CorpusMiner._
    import com.github.woooking.cosyn.Config._

    val filter = CombinedFilter(nodes.map(FragmentFilter))

    startWith(Ready, Result(graphDir.listRecursively.filter(_.isRegularFile).toList, List.empty, ActorNum))

    when(Ready) {
        case Event(AskForTask(result), Result(_, dfgs, aliveActorNum)) if dfgs.size + result.size > limit =>
            sender() ! NoTask
            stay() using Result(Nil, (dfgs ++ result).take(limit), aliveActorNum)
        case Event(AskForTask(result), Result(Nil, dfgs, aliveActorNum)) =>
            sender() ! NoTask
            stay() using Result(Nil, dfgs ++ result, aliveActorNum)
        case Event(AskForTask(result), Result(f :: fs, dfgs, aliveActorNum)) =>
            sender() ! Task(f)
            stay() using Result(fs, dfgs ++ result, aliveActorNum)
        case Event(Terminated(_), Result(files, dfgs, aliveActorNum)) =>
            if (aliveActorNum == 1) stop(FSM.Normal)
            else stay() using Result(files, dfgs, aliveActorNum - 1)
    }

    onTermination {
        case StopEvent(FSM.Normal, _, Result(_, dfgs, _)) =>
            println(s"${self.path.name}: successfully stopped.")
            println(s"Mining from ${dfgs.size} dfgs.")

            val result = filter.filter(Miner.mine(dfgs).sortBy(_.frequency().asInstanceOf[IntFrequency].intValue()))

            result.map(r => r.frequency() -> r.toGraph).foreach(r => {
                println("=====")
                println(r._1)
                GraphUtil.printGraph()(r._2)
            })

            context stop self
            context.system.terminate()
    }

    initialize()

    (0 until ActorNum).foreach(i => {
        val actorRef = context.actorOf(GraphFileReader.props(self, nodes), s"corpus-miner-$i")
        context.watch(actorRef)
    })
}

object CorpusMiner {
    def props(nodes: Seq[DFGNode]): Props = Props(new CorpusMiner(nodes))

    def props(nodes: Seq[DFGNode], limit: Int): Props = Props(new CorpusMiner(nodes, limit))

    val ActorNum = 8
    val graphDir = file"/home/woooking/lab/graphs"

    final case class Init(actorNum: Int, projects: List[ProjectInfo])

    final case class AskForTask(dfgs: Seq[SimpleDFG])

    sealed trait State

    case object Ready extends State

    sealed trait Data

    final case class Result(graphFiles: List[File], dfgs: List[SimpleDFG], aliveActorNum: Int) extends Data

    private val system = ActorSystem("cosyn")

    def main(args: Array[String]): Unit = {
        system.actorOf(CorpusMiner.props(Seq(
            DFGNode(NodeType.MethodInvocation, "java.io.File.exists()")
        ), 100))
    }
}

