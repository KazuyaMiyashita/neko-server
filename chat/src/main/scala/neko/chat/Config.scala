package neko.chat

case class Config(
    db: DBConfig,
    server: ServerConfig,
    applicationSecret: String
)

object Config {
  def fromEnv() = Config(
    db = DBConfig.fromEnv(),
    server = ServerConfig.fromEnv(),
    applicationSecret = sys.env
      .get("APPLICATION_SECRET")
      .getOrElse(throw new RuntimeException("system environment APPLICATION_SECRET is required"))
  )
}

case class DBConfig(
    url: String,
    user: String,
    password: String
)

object DBConfig {
  def fromEnv() = DBConfig(
    sys.env.get("DB_URL").getOrElse(throw new RuntimeException("system environment DB_URL is required")),
    sys.env.get("DB_USER").getOrElse(throw new RuntimeException("system environment DB_USER is required")),
    sys.env.get("DB_PASSWORD").getOrElse(throw new RuntimeException("system environment DB_PASSWORD is required"))
  )
}

case class ServerConfig(port: Int)
object ServerConfig {
  def fromEnv() = ServerConfig(
    sys.env
      .get("SERVER_PORT")
      .getOrElse(throw new RuntimeException("system environment SERVER_PORT is required"))
      .toInt
  )
}
