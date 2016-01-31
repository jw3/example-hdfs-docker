package wiii

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpEntity.Chunked
import akka.http.scaladsl.model.{ContentTypes, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import net.ceedubs.ficus.Ficus._
import spray.json._
import wiii.Messages.{ls, _}
import wiii.awa.WebApi

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}

/**
 * a httpfs like interface to hdfs
 */
object Boot extends App with WebApi with LazyLogging {
    implicit val timeout = Timeout(10 seconds)
    implicit val actorSystem: ActorSystem = ActorSystem("HDFSService")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    override def config: Option[Config] = Option(actorSystem.settings.config)

    val hdfshost = config.get.getAs[String]("hdfs.host").getOrElse("localhost")
    val hdfsport = config.get.getAs[Int]("hdfs.port").getOrElse(50070)

    val filesysActor = actorSystem.actorOf(FileSystemActor.props(hdfshost, hdfsport))


    val download =
        get {
            path(Segment) { path =>
                complete {
                    // verify !dir
                    val source = Source.actorPublisher(StreamingActor.props(hdfshost, hdfsport, path))
                    HttpResponse(entity = Chunked(ContentTypes.`application/octet-stream`, source))
                }
            }
        }

    val upload =
        (put & path(Segment)) { path =>
            complete("stream into hdfs")
        }

    val delete =
        get {
            path("rm" / Segment) { path =>
                complete(s"delete file $path")
            } ~ path("rmdir" / Segment) { path =>
                complete(s"delete dir $path")
            }
        }

    val stat =
        (get & path("stat" / Segment)) { path =>
            complete("stat request")
        }

    val list =
        (get & path("ls" / Segment)) { path =>
            extractRequest { request =>
                val f = (filesysActor ? ls(path))
                        .mapTo[Seq[String]]
                        .map(r => LsResult(r).toJson)
                onComplete(f) { result => complete(result) }
            }
        }

    logger.info("starting Service")
    webstart(download ~ upload ~ delete ~ stat)

    Await.ready(actorSystem.whenTerminated, Duration.Inf)
}
