package neko.chat

case class Config(
    db: DBConfig,
    applicationSecret: String
)

object Config {
  def fromEnv() = Config(
    db = DBConfig.fromEnv(),
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
