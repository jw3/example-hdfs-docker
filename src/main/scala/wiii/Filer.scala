package wiii

import org.apache.hadoop.fs.{Path, FileSystem}

/**
 *
 */
trait Filer {
    def filesys: FileSystem

    def isDirectory(path: String): Boolean = filesys.isDirectory(path)

    // implicits
    implicit def strToHadoopPath(string: String): Path = new Path(string)
}
