package neko.chat

import neko.core.server._
import neko.core.http._
import neko.chat.controller.UserController
import java.time.Clock
import neko.core.jdbc.DBPool
import java.sql.Connection
import neko.chat.repository.UserRepository
import neko.chat.repository.UserRepositoryImpl

object Main extends App {

  val clock = Clock.systemUTC()
  val pool: DBPool = new DBPool {
    override def getConnection(): Connection = ???
  }
  val userRepository: UserRepository = new UserRepositoryImpl(pool, clock)
  val userController                 = new UserController(userRepository)

  val routes = Routes(
    GET  -> "/"     -> (_ => Response(OK, "Hello My Server!")),
    POST -> "/echo" -> (req => Response(OK, req.body)),
    POST -> "/users" -> userController.create
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
