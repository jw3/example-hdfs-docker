package wiii

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity.Chunked
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import wiii.awa.WebApi

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import net.ceedubs.ficus.Ficus._


/**
 * a hand rolled httpfs like interface to hdfs
 */
object Boot extends App with WebApi with LazyLogging {
    implicit val actorSystem: ActorSystem = ActorSystem("HDFSService")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    override def config: Option[Config] = Option(actorSystem.settings.config)

    val hdfshost = config.get.getAs[String]("hdfs.host").getOrElse("localhost")
    val hdfsport = config.get.getAs[Int]("hdfs.port").getOrElse(50070)

    val download =
        (get & pathPrefix("dl")) {
            path("file" / Segment) { path =>
                complete {
                    val source = Source.actorPublisher(StreamingActor.props(hdfshost, hdfsport, path))
                    HttpResponse(entity = Chunked(ContentTypes.`application/octet-stream`, source))
                }
            } ~ path("dir" / Segment) { path =>
                complete(s"read directory at $path")
            }
        }

    val upload =
        (put & path("ul" / Segment)) { path =>
            complete("stream into hdfs")
        }

    val delete =
        (get & pathPrefix("del")) {
            path("file" / Segment) { path =>
                complete(s"delete file $path")
            } ~ path("dir" / Segment) { path =>
                complete(s"delete dir $path")
            }
        }

    val stats =
        (get & pathPrefix("stats")) {
            complete("stats request")
        }


    logger.info("starting Service")
    webstart(download ~ upload ~ delete ~ stats)

    Await.ready(actorSystem.whenTerminated, Duration.Inf)
}
