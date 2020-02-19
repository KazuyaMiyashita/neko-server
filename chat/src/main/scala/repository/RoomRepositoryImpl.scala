package neko.chat.repository

import java.time.Clock
import java.util.UUID
import java.sql.Timestamp

import neko.core.jdbc.{DBPool, ConnectionIO}
import neko.core.jdbc.query._
import neko.chat.entity.Room
import java.sql.ResultSet

class RoomRepositoryImpl(pool: DBPool, clock: Clock) extends RoomRepository {

  override def create(name: String): Either[Throwable, Room] = {
    _create(name).runTx(pool.getConnection())
  }

  override def fetchById(id: String): Option[Room] = {
    _fetchById(id).runTx(pool.getConnection()).toOption.flatten
  }

  override def fetchByName(name: String): Option[Room] = {
    _fetchByName(name).runTx(pool.getConnection()).toOption.flatten
  }

  def _create(name: String): ConnectionIO[Room] = ConnectionIO { conn =>
    val id = UUID.randomUUID().toString
    val query =
      """insert into Rooms(id, name, created_at) values (?, ?, ?);"""
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, id)
    stmt.setString(2, name)
    stmt.setTimestamp(3, Timestamp.from(clock.instant()))
    stmt.executeUpdate()
    Room(id, name)
  }

  def _fetchById(id: String): ConnectionIO[Option[Room]] = ConnectionIO { conn =>
    val query = """select * from Rooms where id = ?"""
    val mapping: ResultSet => Room = row =>
      Room(
        id = row.getString("id"),
        name = row.getString("name")
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, id)
    select(stmt, mapping)(conn)
  }

  def _fetchByName(name: String): ConnectionIO[Option[Room]] = ConnectionIO { conn =>
    val query = """select * from Rooms where name = ?"""
    val mapping: ResultSet => Room = row =>
      Room(
        id = row.getString("id"),
        name = row.getString("name")
      )
    val stmt = conn.prepareStatement(query)
    stmt.setString(1, name)
    select(stmt, mapping)(conn)
  }

}
