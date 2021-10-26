package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class GetTicketUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                       private val ewtObtainer: EwtObtainer,
                       private val ticketRepository: TicketRepository) {


  fun getTicket(ticketId: String): Single<Ticket> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap {
          ewtObtainer.getEWT()
              .flatMap { ewt -> ticketRepository.getTicket(ewt, ticketId) }
        }
        .subscribeOn(Schedulers.io())

  }
}
