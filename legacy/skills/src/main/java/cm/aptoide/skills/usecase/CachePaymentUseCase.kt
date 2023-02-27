package cm.aptoide.skills.usecase

import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.CachedPayment
import cm.aptoide.skills.repository.PaymentLocalStorage
import cm.aptoide.skills.util.EskillsPaymentData
import javax.inject.Inject

class CachePaymentUseCase @Inject constructor(private val paymentLocalStorage: PaymentLocalStorage) {
  operator fun invoke(ticket: CreatedTicket, eskillsPaymentData: EskillsPaymentData) {
    paymentLocalStorage.save(CachedPayment(ticket, eskillsPaymentData))
  }
}
