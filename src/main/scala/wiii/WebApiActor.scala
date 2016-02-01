package wiii

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Sink, Source}
import net.ceedubs.ficus.Ficus._
import wiii.awa.ActorWebApi

/**
 *
 */
object WebApiActor {
    def props() = Props[WebApiActor]
}

class WebApiActor extends Actor with ActorWebApi with Routes {
    override val config = Option(context.system.settings.config)
    val hdfshost = config.get.getAs[String]("hdfs.host").getOrElse("localhost")
    val hdfsport = config.get.getAs[Int]("hdfs.port").getOrElse(50070)


    def receive: Receive = {
        case _ =>
    }

    def filesysStream(path: String): Source[Nothing, ActorRef] = {
        Source.actorPublisher(StreamingActor.props(hdfshost, hdfsport, path))
    }

    def filesysWriter(path: String): Sink[Any, ActorRef] = {
        Sink.actorSubscriber(WritingActor.props(hdfshost, hdfsport, path))
    }

    lazy val filesysActor: ActorRef = {
        context.actorOf(FileSystemActor.props(hdfshost, hdfsport))
    }


    override def preStart(): Unit = {
        logger.info("starting Service")
        webstart(download ~ upload ~ delete ~ stats ~ list)
    }
}
