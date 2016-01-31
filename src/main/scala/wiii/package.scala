import org.apache.hadoop.fs.{LocatedFileStatus, RemoteIterator}


package object wiii {

    object Implicits {
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
