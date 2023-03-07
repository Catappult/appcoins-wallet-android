package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.model.ReferralResult
import cm.aptoide.skills.repository.TicketRepository
import io.reactivex.Single
import javax.inject.Inject

class UseReferralUseCase @Inject constructor (
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
){
  operator fun invoke(referralCode: String): Single<ReferralResult>{
    return ewtObtainer.getEWT()
      .flatMap {ticketRepository.postReferralTransaction(it, referralCode)}
  }
}