package com.asfoundation.wallet.billing.googlepay.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.appcoins.wallet.core.network.microservices.model.GooglePayWebTransaction
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateGooglePayTransactionTopupUseCase @Inject constructor(
  private val walletService: WalletService,
  private val googlePayWebRepository: GooglePayWebRepository,
) {

  operator fun invoke(
    value: String,
    currency: String,
    method: String,
    returnUrl: String,
  ): Single<GooglePayWebTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        googlePayWebRepository.createTransaction(
          value = value,
          currency = currency,
          reference = null,
          walletAddress = address,
          origin = null,
          packageName = BuildConfig.APPLICATION_ID,
          metadata = null,
          method = method,
          sku = null,
          callbackUrl = null,
          transactionType = TOP_UP_TRANSACTION_TYPE,
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          referrerUrl = null,
          returnUrl = returnUrl
        )
      }
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
  }

}