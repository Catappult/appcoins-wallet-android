package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.repository.TicketsRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class CreateTicketUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                          private val ewtObtainer: EwtObtainer,
                          private val ticketsRepository: TicketsRepository) {

  fun createTicket(): Observable<TicketResponse> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap { walletAddress ->
          ewtObtainer.getEWT()
              .flatMap { ewt -> ticketsRepository.createTicket(ewt, walletAddress) }
        }
        .subscribeOn(Schedulers.io())
        .toObservable()

  }
}