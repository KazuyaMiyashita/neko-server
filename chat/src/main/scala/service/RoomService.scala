package neko.chat.service

import java.util.UUID
import java.time.Clock
import neko.core.jdbc.ConnectionIO
import neko.chat.repository.{RoomRepository, UserRepository}
import neko.chat.entity.{User, Room}

// class RoomService(
//   userRepository: UserRepository,
//   roomRepository: RoomRepository,
//   clock: Clock
// ) {

//   def createRoom(user: User, roomName: String): ConnectionIO[Unit] = {
//     val room = Room(UUID.randomUUID(), roomName, clock.instant())
//     val e = for {
//       _ <- roomRepository.fetchByName(roomName)
//   }
// }
