package neko.chat.application.usecase

import neko.core.jdbc.ConnectionIORunner
import neko.chat.application.entity.Token
import neko.chat.application.repository.TokenRepository

class Logout(
    tokenRepository: TokenRepository,
    connectionIORunner: ConnectionIORunner
) {

  def execute(token: Token): Boolean = {
    connectionIORunner.runTx(tokenRepository.deleteToken(token)).get.getOrElse(false)
  }

}
