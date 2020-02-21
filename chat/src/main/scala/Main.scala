package neko.chat

import neko.core.server._
import neko.core.http._
import neko.chat.controller.{AuthController, UserController, MessageController}
import java.time.Clock
import neko.core.jdbc.DBPool
import java.sql.{DriverManager, Connection}
import neko.chat.repository.UserRepository
import neko.chat.repository.UserRepositoryImpl
import neko.chat.repository.{MessageRepository, MessageRepositoryImpl}

object Main extends App {

  val clock = Clock.systemUTC()
  val dbPool: DBPool = new DBPool {
    Class.forName("com.mysql.cj.jdbc.Driver")
    override def getConnection(): Connection = {
      DriverManager.getConnection(
        "jdbc:mysql://localhost:13306/db",
        "root",
        ""
      )
    }
  }
  val userRepository: UserRepository = new UserRepositoryImpl
  val userController = new UserController(
    userRepository,
    dbPool,
    clock
  )
  val authController = new AuthController(
    ???,
    dbPool
  )
  val messageRepository: MessageRepository = new MessageRepositoryImpl
  val messageController = new MessageController(
    messageRepository,
    ???,
    dbPool,
    clock
  )

  val routes = Routes(
    GET  -> "/"            -> (_ => Response(OK, "Hello My Server!")),
    POST -> "/users"       -> userController.create,
    POST -> "/auth/login"  -> authController.login,
    POST -> "/auth/logout" -> authController.logout,
    GET  -> "/messages"    -> messageController.get,
    POST -> "/messages"    -> messageController.post
  )

  val requestHandler: IRequestHandler = new HttpRequestHandler(routes)

  val serverSocketHandler = new ServerSocketHandler(
    requestHandler
  )

  new Thread(serverSocketHandler).start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
