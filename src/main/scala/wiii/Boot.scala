package wiii

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * a httpfs like interface to hdfs using akka streams
 */
object Boot extends App with LazyLogging {
    implicit val actorSystem: ActorSystem = ActorSystem("HDFSService")

    val api = actorSystem.actorOf(WebApiActor.props())

    Await.ready(actorSystem.whenTerminated, Duration.Inf)
}
