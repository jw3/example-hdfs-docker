package wiii

import spray.json.DefaultJsonProtocol

/**
 *
 */
object Messages extends DefaultJsonProtocol {
    case class LoadFile(path: String)

    case class IsDirectory(path: String)
    case class ls(path: String)


    case class LsResult(contents: Seq[String])
    implicit val LsResultProtocol = jsonFormat1(LsResult)
}
