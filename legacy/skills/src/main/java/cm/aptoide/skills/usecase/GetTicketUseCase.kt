package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import com.appcoins.wallet.core.network.eskills.model.QueueIdentifier
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetTicketUseCase @Inject constructor(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository
) {


  operator fun invoke(ticketId: String, queueIdentifier: QueueIdentifier?): Single<Ticket> {
    return walletAddressObtainer.getWalletAddress()
      .flatMap {
        ewtObtainer.getEWT()
          .flatMap { ewt -> ticketRepository.getTicket(ewt, ticketId, queueIdentifier) }
      }
      .subscribeOn(Schedulers.io())

  }
}
