package cm.aptoide.skills.games

import cm.aptoide.skills.model.RoomResponse

interface GameStateListener {
  fun onUpdate(roomResponse: RoomResponse)
  fun onFinishGame(roomResponse: RoomResponse)
}
