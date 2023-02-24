package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import cm.aptoide.skills.model.ReferralResponse
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.util.getErrorCodeAndMessage
import io.reactivex.Single
import javax.inject.Inject

class GetReferralUseCase @Inject constructor(
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
) {

  operator fun invoke(): Single<ReferralResponse> {
    return ewtObtainer.getEWT()
      .flatMap { ewt ->
        ticketRepository.getReferral(ewt)
          .doOnError {
            if (it.getErrorCodeAndMessage().first == 404)
              ticketRepository.createReferral(ewt)
          }
      }
  }
}

