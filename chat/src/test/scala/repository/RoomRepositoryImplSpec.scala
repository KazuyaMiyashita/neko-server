package neko.chat.repository

import org.scalatest._
import java.time.Clock
import neko.chat.entity.Room
import neko.chat.repository.share.TestDBPool

class RoomRepositoryImplSpec extends FlatSpec with Matchers {

  val roomRepository: RoomRepositoryImpl = new RoomRepositoryImpl(TestDBPool, Clock.systemUTC())
  def conn()                             = TestDBPool.getConnection()

  "RoomRepositoryImpl" should "createできる" in {
    val name                          = "room1"
    val room: Either[Throwable, Room] = roomRepository._create(name).runRollback(conn())

    room.isRight shouldEqual true
  }

  "RoomRepositoryImpl" should "fetchByNameできる" in {
    val name = "room1"
    val io = for {
      r1   <- roomRepository._create(name)
      room <- roomRepository._fetchByName(r1.name)
    } yield room
    val room = io.runRollback(conn())

    room.map(_.map(_.name)) shouldEqual Right(Some("room1"))
  }

  "RoomRepositoryImpl" should "fetchByIdできる" in {
    val name = "room1"
    val io = for {
      r1   <- roomRepository._create(name)
      room <- roomRepository._fetchById(r1.id)
    } yield room
    val room = io.runRollback(conn())

    room.map(_.map(_.name)) shouldEqual Right(Some("room1"))
  }

}
