package wiii

import akka.actor.{Actor, Props}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, OneByOneRequestStrategy, RequestStrategy}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import wiii.Implicits._

/**
 *
 */
object WritingActor {
    def props(host: String, port: Int, path: String) = Props(new WritingActor(host, port, path))
}

class WritingActor(host: String, port: Int, path: String) extends Actor with ActorSubscriber with LazyLogging {
    val filesys = {
        val conf = new Configuration()
        conf.set("fs.default.name", s"hdfs://$host:$port")
        FileSystem.get(conf)
    }

    val stream = filesys.create(path, true)

    def receive: Receive = {
        case OnNext(bs: ByteString) =>
            stream.write(bs.toArray)
            logger.trace(s"wrote ${bs.size} to $path")
    }
    protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy
}
