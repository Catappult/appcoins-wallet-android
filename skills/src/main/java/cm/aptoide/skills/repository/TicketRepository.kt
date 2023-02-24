package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.*
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single
import javax.inject.Inject

class TicketRepository @Inject constructor(
  private val ticketApi: TicketApi,
  private val ticketLocalStorage: TicketLocalStorage,
  private val ticketApiMapper: TicketApiMapper
) {

  fun createTicket(
    eskillsPaymentData: EskillsPaymentData, ewt: String, walletAddress: WalletAddress
  ): Single<Ticket> {
    return ticketApi.postTicket(ewt, buildTicketRequest(eskillsPaymentData, walletAddress))
      .map { ticketApiMapper.map(it, eskillsPaymentData.queueId) }
      .onErrorReturn { ticketApiMapper.map(it) }
  }

  private fun buildTicketRequest(
    eskillsPaymentData: EskillsPaymentData, walletAddress: WalletAddress
  ) = TicketRequest(
    eskillsPaymentData.packageName,
    eskillsPaymentData.userId,
    eskillsPaymentData.userName,
    walletAddress.address,
    eskillsPaymentData.metadata,
    eskillsPaymentData.environment,
    eskillsPaymentData.numberOfUsers,
    eskillsPaymentData.price,
    eskillsPaymentData.currency,
    eskillsPaymentData.product,
    eskillsPaymentData.timeout,
    eskillsPaymentData.queueId?.id
  )

  fun getTicket(ewt: String, ticketId: String, queueIdentifier: QueueIdentifier?): Single<Ticket> {
    return ticketApi.getTicket(ewt, ticketId).map { ticketApiMapper.map(it, queueIdentifier) }
      .onErrorReturn { ticketApiMapper.map(it) }
  }

  fun getVerification(ewt: String): Single<EskillsVerification> {
    return ticketApi.getVerification(ewt)
  }

  fun cancelTicket(ewt: String, ticketId: String): Single<TicketResponse> {
    return ticketApi.cancelTicket(ewt, ticketId, TicketApi.Refunded())
  }

  fun getInQueueTicket(
    walletAddress: WalletAddress, eskillsPaymentData: EskillsPaymentData
  ): Single<StoredTicket> {
    return ticketLocalStorage.getTicketInQueue(walletAddress, eskillsPaymentData)
  }

  fun cacheTicket(
    walletAddress: WalletAddress, ticketId: String, eskillsPaymentData: EskillsPaymentData
  ) {
    ticketLocalStorage.saveTicketInQueue(walletAddress, ticketId, eskillsPaymentData)
  }

  fun getReferral(ewt: String): Single<ReferralResponse> {
    return ticketApi.getReferral(ewt)
  }

  fun createReferral(ewt: String): Single<ReferralResponse> {
    return ticketApi.createReferral(ewt)
  }
}
