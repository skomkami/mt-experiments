package pl.edu.agh.config

case class Config(dbConfig: DbConfig, flowsConfig: FlowsConfig)

case class FlowsConfig(parallelism: Int, partitionsCount: Int) {
  def isValid: Boolean =
    parallelism <= partitionsCount && partitionsCount % parallelism == 0

  lazy val partitionAssignment: List[(Int, Set[Int])] = {
    (0 to partitionsCount)
      .grouped(partitionsCount / parallelism)
      .toList
      .map(_.toSet)
      .zipWithIndex
      .map {
        case (partitions, node) => node -> partitions
      }
  }
}

case class DbConfig(driver: String, url: String, user: String, password: String)
