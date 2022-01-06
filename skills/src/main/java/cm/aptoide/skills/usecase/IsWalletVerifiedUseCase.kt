package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import io.reactivex.Single

class IsWalletVerifiedUseCase(
    private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  operator fun invoke(): Single<Boolean> {
    return externalSkillsPaymentProvider.isWalletVerified()
  }
}
