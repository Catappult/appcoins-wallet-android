package cm.aptoide.skills.usecase

import android.content.Context
import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import javax.inject.Inject

class SendUserToTopUpFlowUseCase @Inject constructor(
  private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider
) {
  operator fun invoke(context: Context) {
    externalSkillsPaymentProvider.sendUserToTopUpFlow(context)
  }
}
