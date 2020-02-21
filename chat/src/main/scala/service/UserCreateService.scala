package neko.chat.service

import java.util.UUID
import java.time.Clock

import neko.chat.entity.{User, Auth}
import neko.chat.repository.{UserRepository, AuthRepository}
import neko.core.jdbc.ConnectionIO

class UserCreateService(
    userRepository: UserRepository,
    authRepository: AuthRepository,
    clock: Clock
) {

  import UserCreateService._

  def create(request: UserCreateRequest): ConnectionIO[User] = {
    val hashedPassword = AuthRepository.generateHashedPassword(request.rawPassword, request.loginName)
    val now            = clock.instant()
    val user           = User(UUID.randomUUID(), request.screenName, now)
    val auth           = Auth(request.loginName, hashedPassword, user.id)
    for {
      _ <- userRepository.create(user)
      _ <- authRepository.create(auth)
    } yield user
  }

}

object UserCreateService {

  case class UserCreateRequest(
      screenName: String,
      loginName: String,
      rawPassword: String
  )

}
