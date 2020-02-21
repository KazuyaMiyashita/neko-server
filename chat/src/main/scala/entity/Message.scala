package neko.chat.entity

import java.util.UUID
import java.time.Instant

case class Message(
    id: UUID,
    userId: UUID,
    body: String,
    createdAt: Instant
)
