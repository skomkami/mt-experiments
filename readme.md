### Running Kafka and PostgreSQL with Docker
To run third party components such as Docker and PostgreSQL it is the best to use Docker and Docker Compose.

Both can be obtained with Docker Desktop: https://docs.docker.com/compose/install/compose-desktop/

When Docker Compose has been installed, all third party components can be run with following command (run in project root directory):

```bash
docker-compose up -d
```

### Running application

Prerequisites:
- Java JDK in version 8 or newer
- Scala 2.13.x: https://www.scala-lang.org/download/scala2.html
- Sbt version 1.5.2: https://www.scala-sbt.org/download.html

Application has been written in 3 different versions. Each can be found in separate module (directory).

Before running application it is required to create Kafka topics. Each version has Setup app for that purpose.

To run setup app and then streaming app use commands:

```bash
sbt "project zio" "runMain pl.edu.agh.cars.ZIOSetup"
sbt "project zio" "runMain pl.edu.agh.cars.ZIOMain"
```

For Akka version:
```bash
sbt "project akka" "runMain pl.edu.agh.cars.AkkaSetup"
sbt "project akka" "runMain pl.edu.agh.cars.AkkaMain"
```

For FS2 version:
```bash
sbt "project fs2" "runMain pl.edu.agh.cars.FS2Setup"
sbt "project fs2" "runMain pl.edu.agh.cars.FS2Main"
```