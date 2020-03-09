package neko.chat

import neko.core.server._
import neko.core.http._
import java.time.Clock
import neko.core.jdbc.DBPool
import java.sql.{DriverManager, Connection}
import neko.chat.application.repository.{MessageRepository, TokenRepository, UserRepository}
import neko.chat.application.service.{
  CreateUser,
  CreateUserImpl,
  EditUserInfo,
  FetchUserIdByToken,
  GetMessages,
  Login,
  Logout,
  PostMessage
}
import neko.chat.infra.db.{MessageRepositoryImpl, TokenRepositoryImpl, UserRepositoryImpl}
import neko.chat.controller.{AuthController, UserController, MessageController}

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

  val createUser: CreateUser = new CreateUserImpl(userRepository)
  val editUserInfo           = new EditUserInfo(userRepository)
  val fetchUserIdByToken     = new FetchUserIdByToken(tokenRepository)
  val getMessages            = new GetMessages(messageRepository)
  val login                  = new Login(userRepository, tokenRepository)
  val logout                 = new Logout(tokenRepository)
  val postMessage            = new PostMessage(messageRepository)

  val authController    = new AuthController(fetchUserIdByToken, login, logout)
  val messageController = new MessageController(fetchUserIdByToken, getMessages, postMessage)
  val userController    = new UserController(fetchUserIdByToken, createUser, editUserInfo)

  val application: HttpApplication   = new ChatApplication(userController, authController, messageController)
  val requestHandler: RequestHandler = new HttpRequestHandler(application)

  val serverSocketHandler = new ServerSocketHandler(
    requestHandler
  )

  new Thread(serverSocketHandler).start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
