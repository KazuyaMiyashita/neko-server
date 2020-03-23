package neko.chat

import neko.core.server._
import neko.core.http._
import java.time.Clock
import neko.core.jdbc.DBPool
import java.sql.{DriverManager, Connection}
import neko.chat.application.repository.{MessageRepository, TokenRepository, UserRepository}
import neko.chat.application.usecase.{
  CreateUser,
  CreateUserImpl,
  EditUserInfo,
  EditUserInfoImpl,
  FetchUserIdByToken,
  FetchUserIdByTokenImpl,
  GetMessages,
  GetMessagesImpl,
  Login,
  LoginImpl,
  Logout,
  LogoutImpl,
  PostMessage,
  PostMessageImpl
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

  val createUser: CreateUser                 = new CreateUserImpl(userRepository)
  val editUserInfo: EditUserInfo             = new EditUserInfoImpl(userRepository)
  val fetchUserIdByToken: FetchUserIdByToken = new FetchUserIdByTokenImpl(tokenRepository)
  val getMessages: GetMessages               = new GetMessagesImpl(messageRepository)
  val login: Login                           = new LoginImpl(userRepository, tokenRepository)
  val logout: Logout                         = new LogoutImpl(tokenRepository)
  val postMessage: PostMessage               = new PostMessageImpl(messageRepository)

  val authController    = new AuthController(fetchUserIdByToken, login, logout)
  val messageController = new MessageController(fetchUserIdByToken, getMessages, postMessage)
  val userController    = new UserController(fetchUserIdByToken, createUser, editUserInfo)

  val application: HttpApplication   = new ChatApplication(userController, authController, messageController)
  val requestHandler: RequestHandler = new HttpRequestHandler(application)

  val serverSocketHandler = new ServerSocketHandler(requestHandler, config.server.port)

  serverSocketHandler.start()

  println("press enter to terminate")
  io.StdIn.readLine()
  println("closing...")
  serverSocketHandler.terminate()

}
