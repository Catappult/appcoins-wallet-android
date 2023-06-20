package cm.aptoide.skills.repository

import cm.aptoide.skills.model.RoomApiMapper
import com.appcoins.wallet.core.network.eskills.api.RoomApi
import com.appcoins.wallet.core.network.eskills.model.PatchRoomRequest
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import com.appcoins.wallet.core.network.eskills.model.UserStatus
import io.reactivex.Single
import javax.inject.Inject

class RoomRepository @Inject constructor(
  private val roomApi: RoomApi, private val roomApiMapper: RoomApiMapper
) {
  companion object {
    const val BEARER_ = "Bearer "
  }

  fun patch(session: String, score: Long, status: UserStatus): Single<RoomResponse> {
    val patchRoomRequest = PatchRoomRequest(score, status)
    return roomApiMapper.map(roomApi.patchRoom(BEARER_ + session, patchRoomRequest))
  }

  fun getRoom(session: String): Single<RoomResponse> {
    return roomApiMapper.map(roomApi.getRoom(BEARER_ + session))
  }
}
