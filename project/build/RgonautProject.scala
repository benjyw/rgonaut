import sbt._

class RgonautProject(info: ProjectInfo) extends ParentProject(info) {
  lazy val library = project("library", "rgonaut", new DefaultProject(_) {
    val jackson = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.0"
  })

  Credentials((Path.userHome / ".ivy2" / ".foursquare.credentials").asFile, log)
  val publishTo = "foursquare Public" at "https://repo.foursquare.com/nexus/content/repositories/releases"
}
