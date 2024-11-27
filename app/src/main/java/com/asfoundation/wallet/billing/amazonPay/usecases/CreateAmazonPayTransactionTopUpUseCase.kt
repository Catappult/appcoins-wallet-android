package com.asfoundation.wallet.billing.amazonPay.usecases

import com.appcoins.wallet.core.network.microservices.model.AmazonPayTransaction
import com.appcoins.wallet.core.network.microservices.model.AmazonPrice
import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import io.reactivex.Single
import javax.inject.Inject

class CreateAmazonPayTransactionTopUpUseCase @Inject constructor(
  private val walletService: WalletService,
  private val amazonPayRepository: AmazonPayRepository,
) {

  operator fun invoke(
    price: AmazonPrice,
    chargePermissionId: String?
  ): Single<AmazonPayTransaction> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        amazonPayRepository.createTransaction(
          price = price,
          walletAddress = address,
          packageName = BuildConfig.APPLICATION_ID,
          sku = null,
          callbackUrl = null,
          transactionType = TOP_UP_TRANSACTION_TYPE,
          method = METHOD_AMAZONPAY,
          referrerUrl = null,
          chargePermissionId = chargePermissionId,
          entityOemId = null,
          entityDomain = null,
          entityPromoCode = null,
          userWallet = null,
          origin = null,
          reference = null,
          metadata = null,
          guestWalletId = null
        )
      }
  }

  private companion object {
    private const val TOP_UP_TRANSACTION_TYPE = "TOPUP"
    private const val METHOD_AMAZONPAY= "amazonpay"
  }

}