name := "secure-file-storage"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.typesafe.akka" %% "akka-http"   % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12", // or whatever the latest version is
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  "com.pauldijou" %% "jwt-core" % "0.19.0",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "commons-codec" % "commons-codec" % "1.11",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
//  "junit" % "junit" % "4.12" % Test,
//  "com.novocode" % "junit-interface" % "0.11" % Test exclude("junit", "junit-dep"),
  "org.slf4j" % "slf4j-nop" % "1.6.4"
//  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0"
)