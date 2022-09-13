package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import io.reactivex.Single
import javax.inject.Inject

class IsWalletVerifiedUseCase @Inject constructor(
  private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider
) {
  operator fun invoke(): Single<Boolean> {
    return externalSkillsPaymentProvider.isWalletVerified()
  }
}
