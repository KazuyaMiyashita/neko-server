package neko.chat.entity

import java.util.UUID

case class Auth(
    loginName: String,
    hashedPassword: String,
    userId: UUID
)
