package neko.chat.application.repository

import scala.util.Try

import neko.chat.application.entity.Token
import neko.chat.application.entity.User.UserId

trait TokenRepository {

  def createToken(userId: UserId): Token
  def saveToken(userId: UserId, token: Token): Try[Unit]
  def deleteToken(token: Token): Try[Boolean]
  def fetchUserIdByToken(token: Token): Try[Option[UserId]]

}
