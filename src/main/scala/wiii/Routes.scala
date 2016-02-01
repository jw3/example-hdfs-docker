package wiii

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity.Chunked
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import spray.json._
import wiii.Messages.{LsResult, StatResult, ls, stat, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


/**
 *
 */
trait Routes {
    implicit def materializer: ActorMaterializer

    def filesysActor: ActorRef
    def filesysStream(path: String): Source[Nothing, ActorRef]
    def filesysWriter(path: String): Sink[Any, ActorRef]

    val download =
        (get & path(Segment)) { path =>
            val f = (filesysActor ? IsDirectory(path)).mapTo[Boolean]
            onComplete(f) {
                case Success(false) =>
                    complete(HttpResponse(entity = Chunked(`application/octet-stream`, filesysStream(path))))
                case Success(true) =>
                    failWith(new RuntimeException(s"$path is not a file"))
                case Failure(ex) =>
                    failWith(ex)
            }
        }

    val upload =
        (put & path(Segment)) { path =>
            fileUpload("data") {
                case (metadata, byteSource) =>
                    byteSource.runWith(filesysWriter(path))
                    complete("OK")
            }
        }

    val delete =
        get {
            path("rm" / Segment) { path =>
                complete(s"delete file $path")
            } ~ path("rmdir" / Segment) { path =>
                complete(s"delete dir $path")
            }
        }

    val stats =
        (get & path("stat" / Segment)) { path =>
            val f = (filesysActor ? stat(path))
                    .mapTo[StatResult]
                    .map(r => r.toJson)
            onComplete(f) { res => complete(res) }
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
}
