package wiii

import spray.json.DefaultJsonProtocol

/**
 *
 */
object Messages extends DefaultJsonProtocol {
    case class LoadFile(path: String)
    case class IsDirectory(path: String)

    // ls
    case class ls(path: String)
    case class LsResult(contents: Seq[String])
    implicit val LsResultProtocol = jsonFormat1(LsResult)

    // stat
    case class stat(path: String)
    case class StatResult(name: String, size: Long, owner: String, mod: Long, acc: Long)
    implicit val StatResultProtocol = jsonFormat5(StatResult)
}
