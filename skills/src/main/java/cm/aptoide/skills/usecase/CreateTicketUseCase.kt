package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.util.EskillsUri
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class CreateTicketUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                          private val ewtObtainer: EwtObtainer,
                          private val ticketRepository: TicketRepository,
                          private val networkScheduler: Scheduler) {
  fun getOrCreateWallet(): Observable<String> {
    return walletAddressObtainer.getOrCreateWallet()
  }

  fun createTicket(eskillsUri: EskillsUri): Single<TicketResponse> {
    return walletAddressObtainer.getWalletAddress()
        .subscribeOn(networkScheduler)
        .flatMap { walletAddress ->
          ewtObtainer.getEWT()
              .flatMap { ewt ->
                ticketRepository.createTicket(eskillsUri, ewt, walletAddress)
              }
        }
  }
}

