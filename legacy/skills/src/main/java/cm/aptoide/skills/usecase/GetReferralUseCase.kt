package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.EwtObtainer
import com.appcoins.wallet.core.network.eskills.model.ReferralResponse
import cm.aptoide.skills.repository.TicketRepository
import cm.aptoide.skills.util.getErrorCodeAndMessage
import io.reactivex.Single
import javax.inject.Inject

class GetReferralUseCase @Inject constructor(
  private val ewtObtainer: EwtObtainer,
  private val ticketRepository: TicketRepository,
) {
  var failedReferral = ReferralResponse("ERROR", 0, false)
  operator fun invoke(): Single<ReferralResponse> {
    return ewtObtainer.getEWT()
      .flatMap { ewt ->
        ticketRepository.getReferral(ewt)
          .onErrorReturn {
            return@onErrorReturn if (it.getErrorCodeAndMessage().first == 404)
              ticketRepository.createReferral(ewt)
                .onErrorReturn{ failedReferral }
                .blockingGet()
            else failedReferral
          }
      }
  }
}

