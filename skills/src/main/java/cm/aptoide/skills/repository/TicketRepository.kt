package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.PayTicketRequest
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Single

class TicketRepository(private val ticketApi: TicketApi) {

  fun createTicket(userId: String, ewt: String, walletAddress: String): Single<TicketResponse> {
    return ticketApi.postTicket(
        ewt, buildTicketRequest(userId, walletAddress, ewt))
  }

  fun payTicket(ticketId: String, callbackUrl: String): Single<Any> {
    return ticketApi.createTicket(buildPayTicketRequest(ticketId, callbackUrl))
  }

  private fun buildPayTicketRequest(ticketId: String, callbackUrl: String): PayTicketRequest {
    return PayTicketRequest(ticketId, callbackUrl)
  }

  private fun buildTicketRequest(userId: String, walletAddress: String, ewt: String) =
      TicketRequest("testing", userId, walletAddress, emptyMap(), ewt)
}