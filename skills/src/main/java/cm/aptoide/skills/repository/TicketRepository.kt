package cm.aptoide.skills.repository

import android.util.Log
import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.*
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import javax.inject.Inject

class TicketRepository @Inject constructor(
  private val ticketApi: TicketApi,
  private val ticketLocalStorage: TicketLocalStorage,
  private val ticketApiMapper: TicketApiMapper,
  private val referralMapper: ReferralMapper
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

  fun postReferralTransaction(
    ewt: String,
    referralCode: String
  ): Single<ReferralResult> {
    return ticketApi.postReferralTransaction(ewt, referralCode)
      .subscribeOn(Schedulers.io())
      .map{
        SuccessfulReferral(it) as ReferralResult
      }
      .onErrorReturn {
        referralMapper.mapHttpException(it as HttpException)
      }
  }

  fun getFirstTimeUserCheck(
    ewt: String
  ): Single<Boolean>{
    return ticketApi.getFirstTimeUserCheck(ewt)
      .subscribeOn(Schedulers.io())
      .map{ it.firstTimeUserCheck }
      .onErrorReturn { false }
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
      .subscribeOn(Schedulers.io())
  }

  fun createReferral(ewt: String): Single<ReferralResponse> {
    return ticketApi.createReferral(ewt)
      .subscribeOn(Schedulers.io())
  }
}
