package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import io.reactivex.Single
import javax.inject.Inject

class IsPaypalAgreementCreatedUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(): Single<Boolean> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap { addressModel ->
        payPalV2Repository.getCurrentBillingAgreement(
          walletAddress = addressModel.address,
          walletSignature = addressModel.signedAddress
        )
      }
  }

}
