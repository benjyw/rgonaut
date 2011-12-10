name := "rgonaut"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies += "org.codehaus.jackson" % "jackson-core-asl" % "1.8.0" withSources()

libraryDependencies += "net.liftweb" %% "lift-json" % "2.4-M5" withSources()

// Pull the jars from the ivy cache to lib_managed, so that IntelliJ can see them.
retrieveManaged := true

// Put all the jars directly under lib_managed/jars, lib_managed/srcs. This is because IntelliJ won't recurse under jar directories.
retrievePattern := "[type]s/[artifact](-[revision])(-[classifier]).[ext]"
