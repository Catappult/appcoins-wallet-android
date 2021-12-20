package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.Ticket
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single

class PayTicketUseCase(private val externalSkillsPaymentProvider: ExternalSkillsPaymentProvider) {
  fun pay(ticket: CreatedTicket, eskillsPaymentData: EskillsPaymentData): Single<Ticket> {
    val environment = eskillsPaymentData.environment
    if (environment == EskillsPaymentData.MatchEnvironment.LIVE || environment == null) {
      launchPurchaseFlow(eskillsPaymentData, ticket)
    }
    return Single.just(ticket)
  }

  private fun launchPurchaseFlow(eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket) {
    externalSkillsPaymentProvider.pay(eskillsPaymentData, ticket).subscribe()
  }
}
