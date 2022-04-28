import Dependencies.{io, _}

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(name := "mt-experimetns")
  .aggregate(model, akka, zio, fs2, main)

lazy val model =
  project
    .in(file("model"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(io.github.etspaceman.`scalacheck-faker`)
    )

lazy val common = project
  .in(file("common"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(org.apache.kafka.`kafka-clients`))

lazy val akka =
  project
    .in(file("akka"))
    .dependsOn(model, common)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        com.typesafe.akka.`akka-stream`,
        com.typesafe.akka.`akka-stream-kafka`
      )
    )

lazy val zio =
  project
    .in(file("zio"))
    .dependsOn(model, common)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
//        dev.zio.`zio-streams`,
        dev.zio.`zio-kafka`,
        dev.zio.`zio-json`,
        dev.zio.`zio-interop-cats`,
        com.github.pureconfig.pureconfig
      )
    )

lazy val fs2 =
  project
    .in(file("fs2"))
    .dependsOn(model, common)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        co.fs2.`fs2-core`,
        com.github.fd4s.`fs2-kafka`
      )
    )

lazy val main =
  project
    .in(file("main"))
    .dependsOn(akka, zio, fs2)
    .settings(libraryDependencies ++= Seq(com.github.pureconfig.pureconfig))

lazy val baseLibraries = Seq(
  com.outr.scribe,
  com.softwaremill.quicklens,
  com.beachape.enumeratum,
  com.beachape.`enumeratum-circe`,
  io.circe.`circe-generic`,
  io.circe.`circe-parser`,
  org.scalacheck.scalacheck,
  org.scalatest.scalatest,
  org.typelevel.`discipline-scalatest`,
  org.slf4j.`slf4j-api`,
  org.slf4j.`slf4j-simple`,
  org.tpolecat.`doobie-core`,
  org.tpolecat.`doobie-core`,
  org.tpolecat.`doobie-h2`,
  org.tpolecat.`doobie-postgres`,
  org.tpolecat.`doobie-postgres-circe`
)

lazy val commonSettings = Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty,
  libraryDependencies ++= baseLibraries,
  libraryDependencies ++= baseLibraries.map(_ % Test),
  Compile / console / scalacOptions --= Seq("-Wunused:_", "-Xfatal-warnings"),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value
)

lazy val effects = Seq(org.typelevel.`cats-effect`)
