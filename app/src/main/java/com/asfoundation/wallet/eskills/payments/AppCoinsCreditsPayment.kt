package com.asfoundation.wallet.eskills.payments

import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.FailedPayment
import cm.aptoide.skills.model.PaymentResult
import cm.aptoide.skills.model.SuccessfulPayment
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import com.asfoundation.wallet.ui.iab.RewardPayment
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.ui.iab.Status
import io.reactivex.Single
import javax.inject.Inject

class AppCoinsCreditsPayment
@Inject
constructor(private val rewardsManager: RewardsManager, private val billing: Billing) {
  fun pay(eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket): Single<PaymentResult> {
    return rewardsManager
        .pay(
            eskillsPaymentData.product,
            ticket.ticketPrice,
            eskillsPaymentData.packageName,
            "BDS",
            "ESKILLS",
            null,
            ticket.callbackUrl,
            ticket.ticketId,
            null,
            ticket.productToken)
        .andThen(
            rewardsManager
                .getPaymentStatus(
                    eskillsPaymentData.packageName, eskillsPaymentData.product, ticket.ticketPrice)
                .skipWhile { it.status == Status.PROCESSING })
        .firstOrError()
        .flatMap { paymentStatus: RewardPayment ->
          handlePaymentStatus(paymentStatus, eskillsPaymentData)
        }
  }

  private fun handlePaymentStatus(
      transaction: RewardPayment,
      eskillsPaymentData: EskillsPaymentData
  ): Single<PaymentResult> {
    return when (transaction.status) {
      Status.COMPLETED -> {
        rewardsManager
            .getTransaction(
                eskillsPaymentData.packageName,
                eskillsPaymentData.product,
                eskillsPaymentData.price!!)
            .firstOrError()
            .flatMap { Single.just(SuccessfulPayment) }
      }
      Status.ERROR -> Single.just(FailedPayment.GenericError(transaction.errorMessage))
      Status.FORBIDDEN -> Single.just(FailedPayment.FraudError(transaction.errorMessage))
      Status.NO_NETWORK -> Single.just(FailedPayment.NoNetworkError)
      else -> Single.just(SuccessfulPayment)
    }
  }
}
