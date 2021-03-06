package neko.chat

import neko.core.server._
import neko.core.http._
import java.time.Clock
import neko.core.jdbc.DBPool
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
    override def getConnection(): Connection = {
      DriverManager.getConnection(
        config.db.url,
        config.db.user,
        config.db.password
      )
    }
  }

  val messageRepository: MessageRepository = new MessageRepositoryImpl(dbPool, clock)
  val tokenRepository: TokenRepository     = new TokenRepositoryImpl(dbPool, clock, config.applicationSecret)
  val userRepository: UserRepository       = new UserRepositoryImpl(dbPool, clock, config.applicationSecret)

  val createUser         = new CreateUser(userRepository)
  val editUserInfo       = new EditUserInfo(userRepository)
  val fetchUserIdByToken = new FetchUserIdByToken(tokenRepository)
  val getMessages        = new GetMessages(messageRepository)
  val login              = new Login(userRepository, tokenRepository)
  val logout             = new Logout(tokenRepository)
  val postMessage        = new PostMessage(messageRepository)

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
