package cm.aptoide.skills.model

data class CachedPayment(
  val ticket: CreatedTicket,
  val eskillsPaymentData: com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
)
