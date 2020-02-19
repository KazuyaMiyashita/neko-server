package neko.chat.repository.share

import java.sql.{Connection, DriverManager}
import neko.core.jdbc.DBPool

object TestDBPool extends DBPool {

  Class.forName("com.mysql.cj.jdbc.Driver")

  override def getConnection(): Connection = {
    DriverManager.getConnection(
      "jdbc:mysql://localhost:13306/db",
      "root",
      ""
    )
  }

}
