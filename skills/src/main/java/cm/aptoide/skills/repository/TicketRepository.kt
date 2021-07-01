package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.PayTicketRequest
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single

class TicketRepository(private val ticketApi: TicketApi) {

  fun createTicket(eskillsPaymentData: EskillsPaymentData, ewt: String,
                   walletAddress: String): Single<TicketResponse> {
    return ticketApi.postTicket(ewt, buildTicketRequest(eskillsPaymentData, walletAddress))
  }

  private fun buildTicketRequest(eskillsPaymentData: EskillsPaymentData, walletAddress: String) =
      TicketRequest(
          eskillsPaymentData.packageName, eskillsPaymentData.userId, eskillsPaymentData.userName,
          walletAddress, eskillsPaymentData.metadata, eskillsPaymentData.environment,
          eskillsPaymentData.numberOfUsers, eskillsPaymentData.price, eskillsPaymentData.currency,
          eskillsPaymentData.product
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
