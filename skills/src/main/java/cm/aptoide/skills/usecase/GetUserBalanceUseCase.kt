package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import io.reactivex.Single
import java.math.BigDecimal

class GetUserBalanceUseCase(
    private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  operator fun invoke(): Single<BigDecimal> {
    return externalSkillsPaymentProvider.getBalance()
  }
}
