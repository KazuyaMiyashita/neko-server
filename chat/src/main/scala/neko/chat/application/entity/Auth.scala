package neko.chat.application.entity

import java.util.UUID

case class Auth(
    email: Email,
    hashedPassword: HashedPassword,
    userId: UUID
)
