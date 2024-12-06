package com.asfoundation.wallet.billing.amazonPay.usecases

import com.appcoins.wallet.core.network.microservices.model.AmazonPayCheckoutSessionRequest
import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import io.reactivex.Completable
import javax.inject.Inject

class PatchAmazonPayCheckoutSessionUseCase @Inject constructor(
  private val walletService: WalletService,
  private val amazonPayRepository: AmazonPayRepository,
) {

  operator fun invoke(
    uid: String?,
    amazonPayCheckoutSessionRequest: AmazonPayCheckoutSessionRequest
  ): Completable {
    return walletService.getWalletAddress()
      .flatMapCompletable { address ->
        amazonPayRepository.patchAmazonPayCheckoutSession(
          uid = uid,
          walletAddress = address,
          amazonPayCheckoutSessionRequest = amazonPayCheckoutSessionRequest
        )
      }
  }

}