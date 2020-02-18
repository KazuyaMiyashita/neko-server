package neko.chat.repository

import org.scalatest._
import java.sql.{DriverManager, Connection}
import neko.core.jdbc.DBPool
import java.time.Clock
import neko.chat.entity.User

class UserRepositoryImplSpec extends FlatSpec with Matchers {

  val userRepository: UserRepositoryImpl = new UserRepositoryImpl(TestDBPool, Clock.systemUTC())
  def conn()                             = TestDBPool.getConnection()

  "UserRepositoryImplSpec" should "insertできる" in {
    val name                          = "Alice"
    val user: Either[Throwable, User] = userRepository._insert(name).runRollback(conn())

    1 shouldEqual 1
  }

  "UserRepositoryImplSpec" should "fetchByできる" in {

    1 shouldEqual 1
  }

}

object TestDBPool extends DBPool {

  Class.forName("com.mysql.cj.jdbc.Driver")

  override def getConnection(): Connection = {
    DriverManager.getConnection(
      "jdbc:mysql://localhost:13306/db",
      "root",
      ""
    )
  }

}
