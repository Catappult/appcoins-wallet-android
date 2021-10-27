package cm.aptoide.skills.repository

import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.model.RoomResponse
import io.reactivex.Single

class RoomRepository(private val roomApi: RoomApi) {
  companion object {
    const val BEARER_ = "Bearer "
  }

  fun getRoom(session: String): Single<RoomResponse> {
    return roomApi.getRoom(BEARER_ + session)
  }
}
