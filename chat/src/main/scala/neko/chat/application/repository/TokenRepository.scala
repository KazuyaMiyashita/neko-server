package neko.chat.application.repository

import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId

trait TokenRepository {

  def createToken(userId: UserId): Token
  def saveToken(userId: UserId, token: Token): Unit
  def deleteToken(token: Token): Boolean
  def fetchUserIdByToken(token: Token): Option[UserId]

}

