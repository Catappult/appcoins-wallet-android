package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single

class TicketRepository(
    private val ticketApi: TicketApi,
    private val ticketLocalStorage: TicketLocalStorage,
    private val ticketApiMapper: TicketApiMapper
) {

  fun createTicket(
      eskillsPaymentData: EskillsPaymentData, ewt: String,
      walletAddress: String
  ): Single<Ticket> {
    return ticketApi.postTicket(ewt, buildTicketRequest(eskillsPaymentData, walletAddress))
        .map { ticketApiMapper.map(it) }
        .onErrorReturn { ticketApiMapper.map(it) }
  }

  private fun buildTicketRequest(eskillsPaymentData: EskillsPaymentData, walletAddress: String) =
      TicketRequest(
          eskillsPaymentData.packageName, eskillsPaymentData.userId, eskillsPaymentData.userName,
          walletAddress, eskillsPaymentData.metadata, eskillsPaymentData.environment,
          eskillsPaymentData.numberOfUsers, eskillsPaymentData.price, eskillsPaymentData.currency,
          eskillsPaymentData.product, eskillsPaymentData.timeout
      )

  fun getTicket(ewt: String, ticketId: String): Single<Ticket> {
    return ticketApi.getTicket(ewt, ticketId)
        .map { ticketApiMapper.map(it) }
        .onErrorReturn { ticketApiMapper.map(it) }
  }

  fun cancelTicket(ewt: String, ticketId: String): Single<TicketResponse> {
    return ticketApi.cancelTicket(ewt, ticketId, TicketApi.Refunded())
  }

  fun getInQueueTicket(
      walletAddress: String,
      eskillsPaymentData: EskillsPaymentData
  ): Single<StoredTicket> {
    return ticketLocalStorage.getTicketInQueue(walletAddress, eskillsPaymentData)
  }

  fun cacheTicket(
      walletAddress: String,
      ticketId: String,
      eskillsPaymentData: EskillsPaymentData
  ) {
    ticketLocalStorage.saveTicketInQueue(walletAddress, ticketId, eskillsPaymentData)
  }
}
