package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Single

class TicketRepository(private val ticketApi: TicketApi) {

  fun createTicket(userId: String, ewt: String, walletAddress: String): Single<TicketResponse> {
    return ticketApi.postTicket(
        ewt, buildTicketRequest(userId, walletAddress))
  }

  private fun buildTicketRequest(userId: String, walletAddress: String) =
      TicketRequest(userId, walletAddress, emptyMap())
}