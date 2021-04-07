package cm.aptoide.skills.repository

import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.model.LoginRequest
import cm.aptoide.skills.model.LoginResponse
import io.reactivex.Single

class LoginRepository(private val roomApi: RoomApi) {

  fun login(ewt: String, roomId: String): Single<LoginResponse> {
    return roomApi.login(ewt, LoginRequest(roomId))
  }
}