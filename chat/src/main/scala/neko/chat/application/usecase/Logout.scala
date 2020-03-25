package neko.chat.application.usecase

import neko.chat.application.entity.Token
import neko.chat.application.repository.TokenRepository

class Logout(tokenRepository: TokenRepository) {

  def execute(token: Token): Boolean = {
    tokenRepository.deleteToken(token).getOrElse(false)
  }

}
