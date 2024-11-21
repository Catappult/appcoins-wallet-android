package com.asfoundation.wallet.billing.amazonPay.usecases

import com.appcoins.wallet.core.network.microservices.model.AmazonPayChargePermissionResponse
import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import io.reactivex.Single
import javax.inject.Inject

class GetAmazonPayChargePermissionUseCase @Inject constructor(
  private val amazonPayRepository: AmazonPayRepository,
  private val walletService: WalletService,
  ) {

    operator fun invoke(): Single<AmazonPayChargePermissionResponse> {
      return walletService.getWalletAddress()
        .flatMap { address ->
          amazonPayRepository.getAmazonPayChargePermission(
            walletAddress = address,
          )
        }
    }

}
