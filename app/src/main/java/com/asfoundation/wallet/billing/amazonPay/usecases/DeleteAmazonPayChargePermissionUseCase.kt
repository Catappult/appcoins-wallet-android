package com.asfoundation.wallet.billing.amazonPay.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import io.reactivex.Completable
import javax.inject.Inject

class DeleteAmazonPayChargePermissionUseCase @Inject constructor(
  private val amazonPayRepository: AmazonPayRepository,
  private val walletService: WalletService,
  ) {

    operator fun invoke(): Completable {
      return walletService.getWalletAddress()
        .flatMapCompletable { address ->
          amazonPayRepository.deleteAmazonPayChargePermission(
            walletAddress = address,
          )
        }
    }

}
