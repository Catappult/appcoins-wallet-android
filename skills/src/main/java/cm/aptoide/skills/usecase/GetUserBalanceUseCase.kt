package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class GetUserBalanceUseCase @Inject constructor(
  private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider
) {
  operator fun invoke(): Single<BigDecimal> {
    return externalSkillsPaymentProvider.getBalance()
  }
}
