package pl.edu.agh.config

case class Config(dbConfig: DbConfig)

case class DbConfig(driver: String, url: String, user: String, password: String)
