package cm.aptoide.skills.usecase

import android.content.Context
import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider

class SendUserToTopUpFlowUseCase(
    private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  fun send(context: Context) {
    externalSkillsPaymentProvider.sendUserToTopUpFlow(context)
  }
}
