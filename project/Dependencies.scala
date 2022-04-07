import sbt._

object Dependencies {

  case object Versions {
    val akkaCore = "2.6.18"
    val akkaStreamKafka = "3.0.0"
    val circe = "0.15.0-M1"
    val doobie = "1.0.0-M1"
    val enumeration = "1.7.0"
    val fs2 = "3.2.4"
    val fs2Kafka = "3.0.0-M4"
    val kafkaClients = "3.1.0"
    val quicklens = "1.8.2"
    val pureconfig = "0.14.0"
    val scalaTest = "3.2.9"
    val scribe = "3.6.10"
    val scalacheckFaker = "7.0.0"
//    val zio = "1.0.13"
    val slf4j = "1.7.35"
    val zioCats = "3.1.1.0"
    val zioKafka = "0.15.0"
    val zioJson = "0.1.5"
  }

  case object co {
    case object fs2 {
      val `fs2-core` = "co.fs2" %% "fs2-core" % Versions.fs2
    }
  }

  case object com {

    case object beachape {
      val enumeratum = "com.beachape" %% "enumeratum" % Versions.enumeration
      val `enumeratum-circe` = "com.beachape" %% "enumeratum-circe" % Versions.enumeration
    }

    case object github {
      case object pureconfig {
        val pureconfig =
          "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig
      }

      case object fd4s {
        val `fs2-kafka` = "com.github.fd4s" %% "fs2-kafka" % Versions.fs2Kafka
      }
    }

    case object outr {
      val scribe = "com.outr" %% "scribe" % Versions.scribe
    }

    case object softwaremill {
      val quicklens = "com.softwaremill.quicklens" %% "quicklens" % Versions.quicklens
    }

    case object typesafe {
      case object akka {
        val `akka-actor` =
          "com.typesafe.akka" %% "akka-actor" % Versions.akkaCore
        val `akka-actor-typed` =
          "com.typesafe.akka" %% "akka-actor-typed" % Versions.akkaCore
        val `akka-stream` =
          "com.typesafe.akka" %% "akka-stream" % Versions.akkaCore
        val `akka-stream-kafka` = "com.typesafe.akka" %% "akka-stream-kafka" % Versions.akkaStreamKafka
      }
    }
  }

  case object dev {
    case object zio {
//      val `zio-streams` = "dev.zio" %% "zio-streams" % Versions.zio
      val `zio-kafka` = "dev.zio" %% "zio-kafka" % Versions.zioKafka
      val `zio-json` = "dev.zio" %% "zio-json" % Versions.zioJson
      val `zio-interop-cats` = "dev.zio" %% "zio-interop-cats" % Versions.zioCats
    }
  }

//  case object eu {
//    case object timepit {
//      val refined = "eu.timepit" %% "refined" % Versions.refined
//    }
//  }

  case object io {
    case object circe {
      val `circe-generic` = dependency("generic")
      val `circe-parser` = dependency("parser")
//      val `circe-optics` = dependency("optics")
      val `circe-refined` = dependency("refined")

      private def dependency(artifact: String): ModuleID =
        "io.circe" %% s"circe-$artifact" % Versions.circe
    }
    case object github {
      case object etspaceman {
        val `scalacheck-faker` = "io.github.etspaceman" %% "scalacheck-faker" % Versions.scalacheckFaker
      }
    }
  }

  case object org {
    case object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.15.4"
    }

    case object slf4j {
      val `slf4j-api` = "org.slf4j" % "slf4j-api" % Versions.slf4j
      val `slf4j-simple` = "org.slf4j" % "slf4j-simple" % Versions.slf4j
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % Versions.scalaTest
    }

    case object tpolecat {
      val `doobie-core` = "org.tpolecat" %% "doobie-core" % Versions.doobie
      val `doobie-h2` = "org.tpolecat" %% "doobie-h2" % Versions.doobie
      val `doobie-postgres` = "org.tpolecat" %% "doobie-postgres" % Versions.doobie
      val `doobie-specs2` = "org.tpolecat" %% "doobie-specs2" % Versions.doobie
      val `doobie-postgres-circe` = "org.tpolecat" %% "doobie-postgres-circe" % Versions.doobie
    }

    case object typelevel {
      val `cats-core` =
        "org.typelevel" %% "cats-core" % "2.7.0"

      val `cats-effect` =
        "org.typelevel" %% "cats-effect" % "3.3.5"

      val `discipline-scalatest` =
        "org.typelevel" %% "discipline-scalatest" % "2.1.5"

    }
  }
}
