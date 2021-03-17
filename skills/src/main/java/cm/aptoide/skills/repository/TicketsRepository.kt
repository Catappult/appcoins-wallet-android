package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Single

class TicketsRepository(private val ticketApi: TicketApi) {

  fun createTicket(walletAddress: String): Single<TicketResponse> {
    return ticketApi.postTicket(
        buildTicketRequest(walletAddress))
  }

  private fun buildTicketRequest(walletAddress: String) =
      TicketRequest("string_user_id", walletAddress, "0x3984723948723849", emptyMap())
}