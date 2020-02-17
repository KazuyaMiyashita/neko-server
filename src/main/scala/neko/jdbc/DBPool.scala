package neko.jdbc

import java.sql.Connection

trait DBPool {
  def getConnection(): Connection
}
