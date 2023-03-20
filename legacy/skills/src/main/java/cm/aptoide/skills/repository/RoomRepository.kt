package cm.aptoide.skills.repository

import com.appcoins.wallet.core.network.eskills.api.RoomApi
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import io.reactivex.Single
import javax.inject.Inject

class RoomRepository @Inject constructor(private val roomApi: RoomApi) {
  companion object {
    const val BEARER_ = "Bearer "
  }

  fun getRoom(session: String): Single<RoomResponse> {
    return roomApi.getRoom(BEARER_ + session)
  }
}
