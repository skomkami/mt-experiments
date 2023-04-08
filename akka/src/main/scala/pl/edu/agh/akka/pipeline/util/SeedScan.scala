package pl.edu.agh.akka.pipeline.util

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Attributes.SourceLocation
import akka.stream.*
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

import scala.util.control.NonFatal

case class SeedScan[In, Out](seed: In => Out, f: (Out, In) => Out)
    extends GraphStage[FlowShape[In, Out]] {
  override val shape =
    FlowShape[In, Out](Inlet("SeedScan.in"), Outlet("SeedScan.out"))

  override def initialAttributes: Attributes =
    Attributes.name("SeedScan") and SourceLocation.forLambda(f)

  override def toString: String = "SeedScan"

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with InHandler with OutHandler { self =>

      private var aggregator: Option[Out] = None
      private lazy val decider =
        inheritedAttributes.mandatoryAttribute[SupervisionStrategy].decider

      import Supervision.{Restart, Resume, Stop}
      import shape.{in, out}

      override def onPull(): Unit = pull(in)

      override def onPush(): Unit = {
        try {
          val elem = grab(in)
          aggregator = aggregator.map(f(_, elem)).orElse(Some(seed(elem)))
          aggregator.foreach(push(out, _))
        } catch {
          case NonFatal(ex) =>
            decider(ex) match {
              case Resume => if (!hasBeenPulled(in)) pull(in)
              case Stop   => failStage(ex)
              case Restart =>
                aggregator = None
            }
        }
      }

      setHandlers(in, out, this)
    }
}

object SeedScan {
  implicit class SeedScanOps[In, Out, Mat](flow: Source[In, Mat]) {
    def seedScan(seed: In => Out)(f: (Out, In) => Out): Source[Out, Mat] =
      flow.via(SeedScan(seed, f))
  }
}
