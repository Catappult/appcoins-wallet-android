package cm.aptoide.skills.repository

import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.model.LoginRequest
import cm.aptoide.skills.model.LoginResponse
import io.reactivex.Single
import javax.inject.Inject

class LoginRepository @Inject constructor(private val roomApi: RoomApi) {

  fun login(ewt: String, roomId: String, ticketId: String): Single<LoginResponse> {
    return roomApi.login(ewt, LoginRequest(roomId, ticketId))
  }
}