package com.asfoundation.wallet.skills

import cm.aptoide.skills.BuildConfig
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.WalletAddress
import cm.aptoide.skills.util.EskillsPaymentData
import com.appcoins.wallet.bdsbilling.Billing
import com.asfoundation.wallet.ui.iab.RewardPayment
import com.asfoundation.wallet.ui.iab.RewardsManager
import com.asfoundation.wallet.ui.iab.Status
import io.reactivex.Completable
import io.reactivex.Single

class AppCoinsCreditsPayment(private val rewardsManager: RewardsManager,
                             private val billing: Billing) {
  fun getBalance() = rewardsManager.balance

  fun pay(eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket): Completable {
    return getDeveloperWalletAddress(eskillsPaymentData.packageName)
        .flatMapCompletable { developerAddress: WalletAddress ->
          rewardsManager.pay(
              eskillsPaymentData.product, ticket.ticketPrice, developerAddress.address,
              eskillsPaymentData.packageName, "BDS", "ESKILLS", null, ticket.callbackUrl,
              ticket.ticketId, buildReferralUrl(eskillsPaymentData, ticket), ticket.productToken
          )
              .andThen(
                  rewardsManager.getPaymentStatus(
                      eskillsPaymentData.packageName, eskillsPaymentData.product,
                      ticket.ticketPrice
                  )
                      .takeUntil { it.status != Status.PROCESSING }
              )
              .flatMapCompletable { paymentStatus: RewardPayment ->
                handlePaymentStatus(paymentStatus)
              }
        }
  }

  private fun buildReferralUrl(eskillsPaymentData: EskillsPaymentData,
                               ticket: CreatedTicket): String {
    return (BuildConfig.BASE_HOST + "/transaction/inapp" +
        "?domain=" + eskillsPaymentData.packageName +
        "&callback_url=" + ticket.callbackUrl +
        "&order_reference=" + ticket.ticketId +
        "&product_token=" + ticket.productToken +
        "&product=" + eskillsPaymentData.product +
        "&value=" + eskillsPaymentData.price +
        "&currency=" + eskillsPaymentData.currency)
  }

  private fun getDeveloperWalletAddress(packageName: String): Single<WalletAddress> {
    return billing.getWallet(packageName)
        .map { WalletAddress.fromValue(it) }
  }

  private fun handlePaymentStatus(transaction: RewardPayment): Completable {
    return when (transaction.status) {
      Status.ERROR -> Completable.error(
          AppCoinsCreditsException(transaction.errorMessage)
      )
      Status.FORBIDDEN -> Completable.error(
          AppCoinsCreditsException(transaction.errorMessage)
      )
      Status.NO_NETWORK -> Completable.error(
          AppCoinsCreditsException("No network error.")
      )
      else -> Completable.complete()
    }
  }
}
