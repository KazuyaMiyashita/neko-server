package neko.chat.entity

import java.util.UUID
import java.time.Instant

case class User(
    id: UUID,
    name: String,
    createdAt: Instant
)
