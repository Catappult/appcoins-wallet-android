package cm.aptoide.skills.repository

import com.appcoins.wallet.core.network.eskills.api.RoomApi
import com.appcoins.wallet.core.network.eskills.model.LoginRequest
import io.reactivex.Single
import javax.inject.Inject

class LoginRepository @Inject constructor(private val roomApi: RoomApi) {

  fun login(
    ewt: String,
    roomId: String,
    ticketId: String
  ): Single<com.appcoins.wallet.core.network.eskills.model.LoginResponse> {
    return roomApi.login(
      ewt,
      LoginRequest(roomId, ticketId)
    )
  }
}