package neko.chat.application.usecase

import neko.chat.application.entity.User.UserId
import neko.chat.application.entity.Token
import neko.chat.application.repository.TokenRepository

class FetchUserIdByToken(
    tokenRepository: TokenRepository
) {

  def execute(token: Token): Option[UserId] = {
    tokenRepository.fetchUserIdByToken(token).toOption.flatten
  }

}
