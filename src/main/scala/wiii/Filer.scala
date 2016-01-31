package wiii

import org.apache.hadoop.fs.{Path, FileSystem}
import wiii.Messages.StatResult

/**
 *
 */
trait Filer {
    def filesys: FileSystem

    def isDirectory(path: String): Boolean = filesys.isDirectory(path)
    def fileStats(path: String): StatResult = {
        val stat = filesys.getFileStatus(path)

        val name = stat.getPath.getName
        val size = stat.getLen
        val owner = stat.getOwner
        val modified = stat.getModificationTime
        val accessed = stat.getAccessTime

        StatResult(name, size, owner, modified, accessed)
    }

    // implicits
    implicit def strToHadoopPath(string: String): Path = new Path(string)
}
