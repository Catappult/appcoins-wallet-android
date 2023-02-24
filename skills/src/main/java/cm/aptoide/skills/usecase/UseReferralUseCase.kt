package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.model.ReferralResponse
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single

class UseReferralUseCase (
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
){
  operator fun invoke(referralCode: String): Single<ReferralResponse> {
    return ewtObtainer.getEWT()
      .flatMap {ticketRepository.postReferralTransaction(it, referralCode)}
  }
}