package neko.core.jdbc

import java.sql.Connection

trait DBPool {
  def getConnection(): Connection
}
