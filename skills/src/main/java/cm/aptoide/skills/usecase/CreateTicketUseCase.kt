package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.repository.TicketsRepository
import io.reactivex.Observable

class CreateTicketUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                          private val ticketsRepository: TicketsRepository) {

  fun createTicket(): Observable<TicketResponse> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap { walletAddress ->
          ticketsRepository.createTicket(walletAddress)
        }
        .toObservable()

  }
}