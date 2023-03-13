package cm.aptoide.skills.usecase

import android.util.Log
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
          .onErrorReturn {
            return@onErrorReturn if (it.getErrorCodeAndMessage().first == 404)
              ticketRepository.createReferral(ewt).blockingGet()
            else ReferralResponse("ERROR", 0, false)
          }
      }
  }
}

