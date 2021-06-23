package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.PayTicketRequest
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Single

class TicketRepository(private val ticketApi: TicketApi) {

  fun createTicket(eskillsUri: EskillsUri, ewt: String, walletAddress: String): Single<TicketResponse> {
    return ticketApi.postTicket(ewt, buildTicketRequest(eskillsUri, walletAddress))
  }

  private fun buildTicketRequest(eskillsUri: EskillsUri, walletAddress: String) =
      TicketRequest(
        eskillsUri.getPackageName(), eskillsUri.getUserId(), eskillsUri.getUserName(),
        walletAddress, eskillsUri.getMetadata(), eskillsUri.getEnvironment()!!,
        eskillsUri.getNumberOfUsers(), eskillsUri.getPrice(), eskillsUri.getCurrency(),
        eskillsUri.getProduct(), eskillsUri.getTimeout()
      )

  fun getTicket(ewt: String, ticketId: String): Single<TicketResponse> {
    return ticketApi.getTicket(ewt, ticketId)
  }

  fun cancelTicket(ewt: String, ticketId: String): Single<TicketResponse> {
    return ticketApi.cancelTicket(ewt, ticketId, TicketApi.Refunded())
  }

  fun payTicket(ticketId: String, callbackUrl: String): Single<Any> {
    return ticketApi.createTicket(buildPayTicketRequest(ticketId, callbackUrl))
  }

  private fun buildPayTicketRequest(ticketId: String, callbackUrl: String): PayTicketRequest {
    return PayTicketRequest(ticketId, callbackUrl)
  }
}
