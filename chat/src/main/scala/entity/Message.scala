package neko.chat.entity

import java.time.Instant

case class Message(
    id: String,
    roomId: String,
    userId: String,
    message: String,
    createdAt: Instant
)
