package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.EskillsVerification
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetVerificationUseCase @Inject constructor(
  private val walletAddressObtainer: WalletAddressObtainer,
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository
  ){

  operator fun invoke(): Single<EskillsVerification>{
    return walletAddressObtainer.getWalletAddress().flatMap {
      ewtObtainer.getEWT()
        .flatMap { ewt -> ticketRepository.getVerification(ewt) }
    }.subscribeOn(Schedulers.io())
  }
}