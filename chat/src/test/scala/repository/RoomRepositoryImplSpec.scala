package neko.chat.repository

import org.scalatest._

import java.util.UUID
import java.time.Instant

import neko.chat.entity.Room
import neko.chat.repository.share.TestDBPool

class RoomRepositoryImplSpec extends FlatSpec with Matchers {

  val roomRepository: RoomRepositoryImpl = new RoomRepositoryImpl
  def conn()                             = TestDBPool.getConnection()

  "RoomRepositoryImpl" should "createできる" in {
    val room = Room(UUID.randomUUID(), "room01", Instant.parse("2020-01-01T10:00:00.000Z"))

    val result: Either[Throwable, Unit] = roomRepository.create(room).runRollback(conn())

    result.isRight shouldEqual true
  }

  "RoomRepositoryImpl" should "fetchByNameできる" in {
    val room = Room(UUID.randomUUID(), "room01", Instant.parse("2020-01-01T10:00:00.000Z"))

    val io = for {
      _    <- roomRepository.create(room)
      room <- roomRepository.fetchByName(room.name)
    } yield room
    val result = io.runRollback(conn())

    result shouldEqual Right(Some(room))
  }

  "RoomRepositoryImpl" should "fetchByIdできる" in {
    val room = Room(UUID.randomUUID(), "room01", Instant.parse("2020-01-01T10:00:00.000Z"))

    val io = for {
      _    <- roomRepository.create(room)
      room <- roomRepository.fetchById(room.id)
    } yield room
    val result = io.runRollback(conn())

    result shouldEqual Right(Some(room))
  }

}
