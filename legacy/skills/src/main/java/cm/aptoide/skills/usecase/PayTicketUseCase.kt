package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.PaymentResult
import cm.aptoide.skills.model.SuccessfulPayment
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import io.reactivex.Single
import javax.inject.Inject

class PayTicketUseCase @Inject constructor(private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  operator fun invoke(
    ticket: CreatedTicket,
    eskillsPaymentData: EskillsPaymentData
  ): Single<PaymentResult> {
    val environment = eskillsPaymentData.environment
    if (environment == EskillsPaymentData.MatchEnvironment.LIVE || environment == null) {
      return launchPurchaseFlow(eskillsPaymentData, ticket)
    }
    return Single.just(SuccessfulPayment)
  }

  private fun launchPurchaseFlow(
    eskillsPaymentData: EskillsPaymentData,
    ticket: CreatedTicket
  ): Single<PaymentResult> {
    return externalSkillsPaymentProvider.pay(eskillsPaymentData, ticket)
  }
}
