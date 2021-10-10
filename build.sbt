name := "quicksend"

version := "0.1.1-SNAPSHOT"

scalaVersion := "2.13.6"

idePackagePrefix := Some("io.github.mkotsur.quicksend")

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps"
)

githubTokenSource := TokenSource.Environment("GITHUB_TOKEN")
githubOwner := "mkotsur"
githubRepository := "quicksend"

lazy val backend = (project in file("."))
  .settings(
    libraryDependencies := Seq(
      compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      deps.catsEffect,
      deps.pencil,
      deps.mockitoScala,
      deps.catsTestkit,
      deps.scalaTest
    ) ++ deps.pureConfig ++ deps.logging
  )

lazy val deps = new {

  lazy val V = new {
    val catsEffect = "2.5.3"
    val pureConf = "0.16.0"
    val logback = "1.2.3"
    val log4cats = "1.3.1"
  }

  val pureConfig = Seq(
    "com.github.pureconfig" %% "pureconfig" % V.pureConf,
    "com.github.pureconfig" %% "pureconfig-cats-effect2" % V.pureConf
  )

  val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect

  val logging = Seq(
    "ch.qos.logback" % "logback-core" % V.logback % Test,
    "ch.qos.logback" % "logback-classic" % V.logback % Test,
    "org.typelevel" %% "log4cats-slf4j" % V.log4cats
  )

  val pencil = "com.minosiants" %% "pencil" % "0.6.7"

  val mockitoScala = "org.mockito" %% "mockito-scala" % "1.16.39" % Test
  val catsTestkit = "com.codecommit" %% "cats-effect-testing-scalatest" % "0.5.4" % Test
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9" % Test

}
