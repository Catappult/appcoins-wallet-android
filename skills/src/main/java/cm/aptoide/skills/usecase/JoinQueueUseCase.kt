package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.ProcessingStatus
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.model.WalletAddress
import cm.aptoide.skills.repository.EmptyStoredTicket
import cm.aptoide.skills.repository.StoredTicket
import cm.aptoide.skills.repository.StoredTicketInQueue
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class JoinQueueUseCase @Inject constructor(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
) {

  operator fun invoke(eskillsPaymentData: EskillsPaymentData): Single<Ticket> {
    return walletAddressObtainer.getWalletAddress()
      .subscribeOn(Schedulers.io())
      .flatMap { walletAddress ->
        ewtObtainer.getEWT()
          .flatMap { ewt ->
            ticketRepository.getInQueueTicket(walletAddress, eskillsPaymentData)
              .flatMap {
                createOrResumeTicket(it, eskillsPaymentData, ewt, walletAddress)
              }
          }
      }
      .doOnSuccess { ticket: Ticket ->
        if (ticket is CreatedTicket) {
          ticketRepository.cacheTicket(
            ticket.walletAddress, ticket.ticketId, eskillsPaymentData
          )
        }
      }
  }

  private fun createOrResumeTicket(
    storedTicket: StoredTicket,
    eskillsPaymentData: EskillsPaymentData,
    ewt: String,
    walletAddress: WalletAddress
  ): Single<Ticket> {
    return when (storedTicket) {
      EmptyStoredTicket -> ticketRepository.createTicket(
        eskillsPaymentData, ewt, walletAddress
      )
      is StoredTicketInQueue -> resumeTicketIfPossible(
        ewt, storedTicket, eskillsPaymentData, walletAddress
      )
    }
  }

  private fun resumeTicketIfPossible(
    ewt: String,
    ticketInQueue: StoredTicketInQueue,
    eskillsPaymentData: EskillsPaymentData,
    walletAddress: WalletAddress,
  ): Single<Ticket> {
    return ticketRepository.getTicket(ewt, ticketInQueue.ticketId, eskillsPaymentData.queueId)
      .flatMap {
        if (it is CreatedTicket && it.processingStatus == ProcessingStatus.IN_QUEUE) {
          Single.just(it)
        } else {
          ticketRepository.createTicket(eskillsPaymentData, ewt, walletAddress)
        }
      }
      .onErrorResumeNext {
        ticketRepository.createTicket(eskillsPaymentData, ewt, walletAddress)
      }
  }
}

