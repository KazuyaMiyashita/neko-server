package neko.chat

import neko.core.server._
import neko.core.http._
import java.time.Clock
import neko.core.jdbc.DBPool
import java.sql.{DriverManager, Connection}
import neko.chat.repository.{AuthRepository, AuthRepositoryImpl}
import neko.chat.repository.{UserRepository, UserRepositoryImpl}
import neko.chat.repository.{MessageRepository, MessageRepositoryImpl}
import neko.chat.auth.{Authenticator, AuthenticatorImpl}
import neko.chat.controller.{AuthController, UserController, MessageController}
import neko.chat.service.UserCreateService

object Main extends App {

  val config = Config.fromEnv()
  val clock  = Clock.systemUTC()
  val dbPool: DBPool = new DBPool {
    Class.forName("com.mysql.cj.jdbc.Driver")
    override def getConnection(): Connection = {
      DriverManager.getConnection(
        config.db.url,
        config.db.user,
        config.db.password
      )
    }
  }
  val authRepository: AuthRepository       = new AuthRepositoryImpl(clock)
  val userRepository: UserRepository       = new UserRepositoryImpl
  val messageRepository: MessageRepository = new MessageRepositoryImpl
  val userCreateService                    = new UserCreateService(userRepository, authRepository, clock)
  val authenticator: Authenticator         = new AuthenticatorImpl(authRepository, dbPool)
  val authController = new AuthController(
    authRepository,
    dbPool
  )
  val userController = new UserController(
    userCreateService,
    dbPool,
    clock
  )
  val messageController = new MessageController(
    messageRepository,
    authenticator,
    dbPool,
    clock
  )

  val routes = Routes(
    GET  -> "/"             -> (_ => HttpResponse(OK, "Hello My Server!")),
    POST -> "/users"        -> userController.create,
    POST -> "/auth/login"   -> authController.login,
    POST -> "/auth/logout"  -> authController.logout,
    GET  -> "/auth/session" -> authController.session,
    GET  -> "/messages"     -> messageController.get,
    POST -> "/messages"     -> messageController.post
  )

  val requestHandler: RequestHandler = new HttpRequestHandler(routes)

  val serverSocketHandler = new ServerSocketHandler(
    requestHandler
  )

  new Thread(serverSocketHandler).start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
