package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import com.appcoins.wallet.core.network.microservices.model.PaypalTransaction
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalTransactionTopupUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(
    value: String,
    currency: String,
  ): Single<PaypalTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        payPalV2Repository.createTransaction(
          value = value,
          currency = currency,
          reference = null,
          walletAddress = address,
          origin = null,
          packageName = BuildConfig.APPLICATION_ID,
          metadata = null,
          sku = null,
          callbackUrl = null,
          transactionType = TOP_UP_TRANSACTION_TYPE,
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          referrerUrl = null
        )
      }
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
  }

}