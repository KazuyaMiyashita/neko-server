package neko.chat.repository

import java.util.UUID
import java.sql.Timestamp

import neko.core.jdbc.ConnectionIO
import neko.core.jdbc.query._
import neko.chat.entity.Room
import java.sql.ResultSet

class RoomRepositoryImpl extends RoomRepository {

  import RoomRepositoryImpl._

  override def create(room: Room): ConnectionIO[Unit] = ConnectionIO { conn =>
    val query =
      """insert into Rooms(id, name, created_at) values (?, ?, ?);"""
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, room.id.toString)
    pstmt.setString(2, room.name.toString)
    pstmt.setTimestamp(3, Timestamp.from(room.createdAt))
    pstmt.executeUpdate()
    ()
  }

  override def fetchById(id: UUID): ConnectionIO[Option[Room]] = ConnectionIO { conn =>
    val query = """select * from Rooms where id = ?;"""
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, id.toString)
    select(pstmt, toRoomEntity)(conn)
  }

  override def fetchByName(name: String): ConnectionIO[Option[Room]] = ConnectionIO { conn =>
    val query = """select * from Rooms where name = ?;"""
    val pstmt = conn.prepareStatement(query)
    pstmt.setString(1, name)
    select(pstmt, toRoomEntity)(conn)
  }

}

object RoomRepositoryImpl {

  val toRoomEntity: ResultSet => Room = row =>
    Room(
      id = UUID.fromString(row.getString("id")),
      name = row.getString("name"),
      createdAt = row.getTimestamp("created_at").toInstant
    )

}
