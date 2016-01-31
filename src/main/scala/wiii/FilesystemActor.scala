package wiii

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import wiii.Messages.{IsDirectory, ls}

object FileSystemActor {
    def props(host: String, port: Int) = Props(new FileSystemActor(host, port))
}

class FileSystemActor(host: String, port: Int) extends Actor with Filer with LazyLogging {
    val filesys = {
        val conf = new Configuration()
        conf.set("fs.default.name", s"hdfs://$host:$port")
        FileSystem.get(conf)
    }

    def receive: Receive = {
        case IsDirectory(path) => sender() ! isDirectory(path)
        case ls(path) =>
            sender ! (isDirectory(path) match {
                case true =>
                    import Implicits._
                    filesys.listFiles(path, false).map(_.getPath.getName).toSeq
                case false =>
                    new IllegalArgumentException(s"$path is not a directory")
            })
    }
}