package neko.chat.repository

import neko.chat.entity.Room

trait RoomRepository {

  def create(name: String): Either[Throwable, Room]

  def fetchById(id: String): Option[Room]

  def fetchByName(name: String): Option[Room]

}
