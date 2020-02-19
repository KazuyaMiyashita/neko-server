package neko.chat.entity

import java.util.UUID
import java.time.Instant

case class Room(
    id: UUID,
    name: String,
    createdAt: Instant
)
