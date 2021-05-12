package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.PayTicketRequest
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Single

class TicketRepository(private val ticketApi: TicketApi) {

  fun createTicket(packageName: String, userId: String, userName: String, ewt: String,
                   walletAddress: String, roomMetadata: Map<String, String>,
                   environment: EskillsUri.MatchEnvironment): Single<TicketResponse> {
    return ticketApi.postTicket(
        ewt, buildTicketRequest(packageName, userId, userName, walletAddress, roomMetadata, environment))
  }

  fun getTicket(ewt: String, ticketId: String): Single<TicketResponse> {
    return ticketApi.getTicket(ewt, ticketId)
  }

  fun payTicket(ticketId: String, callbackUrl: String): Single<Any> {
    return ticketApi.createTicket(buildPayTicketRequest(ticketId, callbackUrl))
  }

  private fun buildPayTicketRequest(ticketId: String, callbackUrl: String): PayTicketRequest {
    return PayTicketRequest(ticketId, callbackUrl)
  }

  private fun buildTicketRequest(packageName: String, userId: String, userName: String,
                                 walletAddress: String, roomMetadata: Map<String, String>,
                                 environment: EskillsUri.MatchEnvironment) =
      TicketRequest(packageName, userId, userName, walletAddress, roomMetadata, environment)
}