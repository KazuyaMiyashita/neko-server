package neko.chat.application.repository

import neko.core.jdbc.ConnectionIO
import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId

trait TokenRepository {

  def createToken(userId: UserId): Token
  def saveToken(userId: UserId, token: Token): ConnectionIO[Nothing, Unit]
  def deleteToken(token: Token): ConnectionIO[Nothing, Boolean]
  def fetchUserIdByToken(token: Token): ConnectionIO[Nothing, Option[UserId]]

}
