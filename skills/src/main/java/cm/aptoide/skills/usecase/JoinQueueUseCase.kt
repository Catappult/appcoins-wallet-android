package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.model.TicketStatus
import cm.aptoide.skills.repository.EmptyStoredTicket
import cm.aptoide.skills.repository.StoredTicketInQueue
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Scheduler
import io.reactivex.Single

class JoinQueueUseCase(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
  private val networkScheduler: Scheduler
) {

  fun joinQueue(eskillsPaymentData: EskillsPaymentData): Single<TicketResponse> {
    return walletAddressObtainer.getWalletAddress()
      .subscribeOn(networkScheduler)
      .flatMap { walletAddress ->
        ewtObtainer.getEWT()
          .flatMap { ewt ->
            ticketRepository.getInQueueTicket(walletAddress, eskillsPaymentData)
              .flatMap {
                when (it) {
                  EmptyStoredTicket -> ticketRepository.createTicket(
                    eskillsPaymentData,
                    ewt,
                    walletAddress
                  )
                  is StoredTicketInQueue -> resumeTicketIfPossible(
                    ewt, it, eskillsPaymentData, walletAddress
                  )
                }
              }
          }
      }.doOnSuccess {
        ticketRepository.cacheTicket(
          it.walletAddress,
          it.ticketId,
          eskillsPaymentData
        )
      }
  }

  private fun resumeTicketIfPossible(
    ewt: String,
    ticketInQueue: StoredTicketInQueue,
    eskillsPaymentData: EskillsPaymentData,
    walletAddress: String
  ) = ticketRepository.getTicket(ewt, ticketInQueue.ticketId)
    .flatMap {
      if (it.ticketStatus == TicketStatus.IN_QUEUE) {
        Single.just(it)
      } else {
        ticketRepository.createTicket(
          eskillsPaymentData,
          ewt,
          walletAddress
        )
      }
    }.onErrorResumeNext {
      ticketRepository.createTicket(
        eskillsPaymentData,
        ewt,
        walletAddress
      )
    }
}

