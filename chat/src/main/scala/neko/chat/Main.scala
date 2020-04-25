package neko.chat

import scala.util.Try
import neko.core.server._
import neko.core.http._
import java.time.Clock
import neko.core.jdbc.{DBPool, ConnectionIORunner, DefaultConnectionIORunner}
import java.sql.{DriverManager, Connection}

import neko.chat.application.repository.{MessageRepository, TokenRepository, UserRepository}
import neko.chat.application.usecase.{
  CreateUser,
  EditUserInfo,
  FetchUserIdByToken,
  GetMessages,
  Login,
  Logout,
  PostMessage
}
import neko.chat.infra.db.{MessageRepositoryImpl, TokenRepositoryImpl, UserRepositoryImpl}
import neko.chat.controller.{ControllerComponent, Routing, AuthController, UserController, MessageController}

object Main extends App {

  val config = Config.fromEnv()
  val clock  = Clock.systemUTC()
  val dbPool: DBPool = new DBPool {
    Class.forName("com.mysql.cj.jdbc.Driver")
    override def getConnection(): Try[Connection] = Try {
      DriverManager.getConnection(
        config.db.url,
        config.db.user,
        config.db.password
      )
    }
  }
  val connectionIORunner: ConnectionIORunner = new DefaultConnectionIORunner(dbPool)

  val messageRepository: MessageRepository = new MessageRepositoryImpl(clock)
  val tokenRepository: TokenRepository     = new TokenRepositoryImpl(clock, config.applicationSecret)
  val userRepository: UserRepository       = new UserRepositoryImpl(clock, config.applicationSecret)

  val createUser         = new CreateUser(userRepository, connectionIORunner)
  val editUserInfo       = new EditUserInfo(userRepository, connectionIORunner)
  val fetchUserIdByToken = new FetchUserIdByToken(tokenRepository, connectionIORunner)
  val getMessages        = new GetMessages(messageRepository, connectionIORunner)
  val login              = new Login(userRepository, tokenRepository, connectionIORunner)
  val logout             = new Logout(tokenRepository, connectionIORunner)
  val postMessage        = new PostMessage(messageRepository, connectionIORunner)

  val controllerConponent: ControllerComponent = ControllerComponent.create(config.server.origin)
  val authController                           = new AuthController(fetchUserIdByToken, login, logout, controllerConponent)
  val messageController                        = new MessageController(fetchUserIdByToken, getMessages, postMessage, controllerConponent)
  val userController                           = new UserController(fetchUserIdByToken, createUser, editUserInfo, controllerConponent)

  val application: HttpApplication   = new Routing(userController, authController, messageController, controllerConponent)
  val requestHandler: RequestHandler = new HttpRequestHandler(application)

  val serverSocketHandler = new ServerSocketHandler(requestHandler, config.server.port)

  serverSocketHandler.start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
