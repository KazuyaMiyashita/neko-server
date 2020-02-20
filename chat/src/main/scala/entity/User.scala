package neko.chat.entity

import java.util.UUID
import java.time.Instant

case class User(
    id: UUID,
    screenName: String,
    createdAt: Instant
)
