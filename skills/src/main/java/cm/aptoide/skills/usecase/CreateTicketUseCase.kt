package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class CreateTicketUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                          private val ewtObtainer: EwtObtainer,
                          private val ticketRepository: TicketRepository) {

  fun createTicket(packageName: String, userId: String, userName: String): Single<TicketResponse> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap { walletAddress ->
          ewtObtainer.getEWT()
              .flatMap { ewt ->
                ticketRepository.createTicket(packageName,
                    userId, userName, ewt,
                    walletAddress)
              }
        }
        .subscribeOn(Schedulers.io())

  }
}