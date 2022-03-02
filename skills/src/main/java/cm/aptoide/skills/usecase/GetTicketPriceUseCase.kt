package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.model.Price
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class GetTicketPriceUseCase @Inject constructor(private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  fun getLocalPrice(value: BigDecimal, currency: String): Single<Price> {
    return externalSkillsPaymentProvider.getLocalFiatAmount(value, currency)
  }

  fun getAppcPrice(value: BigDecimal, currency: String): Single<Price> {
    return externalSkillsPaymentProvider.getFiatToAppcAmount(value, currency)
  }

  fun getAppcFormatted(value: BigDecimal, currency: String): Single<String> {
    return externalSkillsPaymentProvider.getFormattedAppcAmount(value, currency)
  }
}
