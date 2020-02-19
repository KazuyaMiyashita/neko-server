package neko.chat.repository

import neko.core.jdbc.ConnectionIO
import neko.chat.entity.Room
import java.util.UUID

trait RoomRepository {

  def create(room: Room): ConnectionIO[Unit]

  def fetchById(id: UUID): ConnectionIO[Option[Room]]

  def fetchByName(name: String): ConnectionIO[Option[Room]]

}