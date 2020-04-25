package neko.chat.application.usecase

import neko.core.jdbc.ConnectionIORunner
import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Token
import neko.chat.application.repository.TokenRepository

class FetchUserIdByToken(
    tokenRepository: TokenRepository,
    connectionIORunner: ConnectionIORunner
) {

  def execute(token: Token): Option[UserId] = {
    connectionIORunner.runReadOnly(tokenRepository.fetchUserIdByToken(token)).get.toOption.flatten
  }

}
