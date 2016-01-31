package wiii

import java.io.FileNotFoundException

import akka.actor.Props
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{Path, FileSystem}


object StreamingActor {
    def props(host: String, port: Int, path: String) = Props(new StreamingActor(host, port, path))
}
class StreamingActor(host: String, port: Int, path: String) extends ActorPublisher[ByteString]  with LazyLogging {
    val filesys = {
        val conf = new Configuration()
        conf.set("fs.default.name", s"hdfs://$host:$port")
        FileSystem.get(conf)
    }

    val chunkSize = 1024
    val arr = Array.ofDim[Byte](chunkSize)

    def receive: Receive = {
        case Request(cnt) =>
            val uri = new Path(path)
            uri match {
                case p if !filesys.exists(p) => throw new FileNotFoundException(s"$p does not exist")
                case p if !filesys.getFileStatus(p).isFile => throw new FileNotFoundException(s"$p is not a file")
                case p =>
                    val is = filesys.open(p)
                    val readBytes = is.read(arr)
                    onNext(ByteString.fromArray(arr, 0, readBytes))
            }
        case Cancel => context.stop(self)
        case _ =>
    }
}
