package neko.core.jdbc

import java.sql.{PreparedStatement, ResultSet, Connection}

object query {

  def select[T](ps: PreparedStatement, mapping: ResultSet => T)(conn: Connection): Option[T] = {
    val resultSet: ResultSet = ps.executeQuery()
    Option.when(resultSet.next())(mapping(resultSet))
  }

}
