package neko.chat.application.entity

import neko.chat.application.entity.User.UserId

case class Auth(
    email: Email,
    hashedPassword: HashedPassword,
    userId: UserId
)
