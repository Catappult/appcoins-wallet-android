package com.asfoundation.wallet.billing.paypal

import com.appcoins.wallet.bdsbilling.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.PayPalV2Repository
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalAgreementUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(token: String): Single<PaypalV2CreateAgreementResponse> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap { addressModel ->
        payPalV2Repository.createBillingAgreement(
          walletAddress = addressModel.address,
          walletSignature = addressModel.signedAddress,
          token = token
        )
      }
  }

}
