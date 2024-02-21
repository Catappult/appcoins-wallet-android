package com.asfoundation.wallet.billing.wallet_one.usecases

import com.appcoins.wallet.core.network.microservices.model.WalletOneTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.wallet_one.repository.WalletOneRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateWalletOneTransactionTopupUseCase @Inject constructor(
  private val walletService: WalletService,
  private val walletOneRepository: WalletOneRepository,
) {

  operator fun invoke(
    value: String,
    currency: String,
    method: String,
    successUrl: String,
    failUrl: String,
  ): Single<WalletOneTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        walletOneRepository.createTransaction(
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
          successUrl = successUrl,
          failUrl = failUrl
        )
      }
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
  }

}
