package cm.aptoide.skills.usecase

import android.util.Log
import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TicketResponse
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class CancelTicketUseCase(private val walletAddressObtainer: WalletAddressObtainer,
                          private val ewtObtainer: EwtObtainer,
                          private val ticketRepository: TicketRepository) {
  fun cancelTicket(ticketId: String): Single<TicketResponse> {
    Log.d("SkillsFragment", ticketId)
    return walletAddressObtainer.getWalletAddress()
        .flatMap {
          ewtObtainer.getEWT()
              .flatMap {
                ewt -> ticketRepository.cancelTicket(ewt, ticketId)
              }
        }
        .subscribeOn(Schedulers.io())
  }
}