package com.asfoundation.wallet.billing.paypal

import com.appcoins.wallet.bdsbilling.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.PayPalV2Repository
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalTokenUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(): Single<PaypalV2CreateTokenResponse> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap { addressModel ->
        val returnUrl = "${PaypalReturnSchemas.RETURN.schema}${BuildConfig.APPLICATION_ID}"
        val cancelUrl = "${PaypalReturnSchemas.CANCEL.schema}${BuildConfig.APPLICATION_ID}"
        payPalV2Repository.createToken(
          walletAddress = addressModel.address,
          walletSignature = addressModel.signedAddress,
          returnUrl = returnUrl,
          cancelUrl = cancelUrl
        )
      }
  }

}
