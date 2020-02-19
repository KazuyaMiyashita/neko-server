package neko.chat.repository

import org.scalatest._

import java.time.Clock
import neko.chat.entity.User
import neko.chat.repository.share.TestDBPool

class UserRepositoryImplSpec extends FlatSpec with Matchers {

  val userRepository: UserRepositoryImpl = new UserRepositoryImpl(TestDBPool, Clock.systemUTC())
  def conn()                             = TestDBPool.getConnection()

  "UserRepositoryImpl" should "insertできる" in {
    val name                          = "Alice"
    val user: Either[Throwable, User] = userRepository._insert(name).runRollback(conn())

    user.isRight shouldEqual true
  }

  "UserRepositoryImpl" should "fetchByできる" in {
    val name = "Alice"
    val io = for {
      u1   <- userRepository._insert(name)
      user <- userRepository._fetchBy(u1.id)
    } yield user
    val user = io.runRollback(conn())

    user.map(_.map(_.name)) shouldEqual Right(Some("Alice"))
  }

}
