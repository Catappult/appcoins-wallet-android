package cm.aptoide.skills.api

import cm.aptoide.skills.model.LoginRequest
import cm.aptoide.skills.model.LoginResponse
import cm.aptoide.skills.model.RoomResponse
import io.reactivex.Single
import retrofit2.http.*

interface RoomApi {

  @GET("room/")
  fun getRoomByTicketId(@Header("authorization") authorization: String,
                        @Query("ticket_id") ticketId: String): Single<RoomResponse>

  @POST("room/authorization/login")
  fun login(@Header("authorization") authorization: String,
            @Body loginRequest: LoginRequest): Single<LoginResponse>
}
