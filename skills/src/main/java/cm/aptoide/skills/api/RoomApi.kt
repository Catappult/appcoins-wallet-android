package cm.aptoide.skills.api

import cm.aptoide.skills.model.RoomResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface RoomApi {

  @GET("room")
  fun getRoom(@Header("authorization") authorization: String,
              @Query("ticket_id") ticketId: String,
              @Query("wallet_address") walletAddress: String): Single<RoomResponse>
}
