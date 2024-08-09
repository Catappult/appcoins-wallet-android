package com.asfoundation.wallet.billing.true_layer.usecases

import com.appcoins.wallet.core.network.microservices.model.TrueLayerTransaction
import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.true_layer.repository.TrueLayerRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateTrueLayerTransactionTopupUseCase @Inject constructor(
  private val walletService: WalletService,
  private val trueLayerWebRepository: TrueLayerRepository,
) {

  operator fun invoke(
    value: String,
    currency: String,
    method: String,
  ): Single<TrueLayerTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        trueLayerWebRepository.createTransaction(
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
        )
      }
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
  }

}