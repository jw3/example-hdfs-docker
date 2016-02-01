import akka.util.Timeout
import org.apache.hadoop.fs.{Path, LocatedFileStatus, RemoteIterator}

import scala.concurrent.duration.DurationInt

package object wiii {
    implicit val timeout = Timeout(10 seconds)

    object Implicits {
        implicit def strToHadoopPath(string: String): Path = new Path(string)

        implicit def remoteIteratorToIterable(iter: RemoteIterator[LocatedFileStatus]): Iterable[LocatedFileStatus] = {
            new Iterable[LocatedFileStatus] {
                def iterator: Iterator[LocatedFileStatus] = new Iterator[LocatedFileStatus] {
                    def hasNext: Boolean = iter.hasNext
                    def next(): LocatedFileStatus = iter.next()
                }
            }
        }
    }
}
