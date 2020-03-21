package neko.chat.application.service

import neko.chat.application.entity.Token
import neko.chat.application.repository.TokenRepository

trait Logout {
  def execute(token: Token): Boolean
}

class LogoutImpl(tokenRepository: TokenRepository) extends Logout {

  def execute(token: Token): Boolean = {
    tokenRepository.deleteToken(token).getOrElse(false)
  }

}
