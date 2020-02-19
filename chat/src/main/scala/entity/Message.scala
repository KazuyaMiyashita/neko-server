package neko.chat.entity

import java.util.UUID
import java.time.Instant

case class Message(
    id: UUID,
    roomId: UUID,
    userId: UUID,
    message: String,
    createdAt: Instant
)
