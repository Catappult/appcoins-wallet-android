package cm.aptoide.skills.usecase

import android.content.Context
import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider

class SendUserVerificationFlowUseCase(
    private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  operator fun invoke(context: Context) {
    externalSkillsPaymentProvider.sendUserToVerificationFlow(context)
  }

}