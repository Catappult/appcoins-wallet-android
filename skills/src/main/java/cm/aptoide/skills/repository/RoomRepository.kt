package cm.aptoide.skills.repository

import cm.aptoide.skills.api.RoomApi
import cm.aptoide.skills.model.RoomResponse
import io.reactivex.Single

class RoomRepository(private val roomApi: RoomApi) {

  fun getRoomByTicketId(ewt: String, ticketId: String,
                        walletAddress: String): Single<RoomResponse> {
    return roomApi.getRoomByTicketId(ewt, ticketId)
  }
}