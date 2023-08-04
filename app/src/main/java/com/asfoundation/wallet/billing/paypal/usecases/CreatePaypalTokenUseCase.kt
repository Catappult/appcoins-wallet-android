package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateToken
import com.asfoundation.wallet.billing.paypal.PaypalReturnSchemas
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalTokenUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(): Single<PaypalCreateToken> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        val returnUrl = "${PaypalReturnSchemas.RETURN.schema}${BuildConfig.APPLICATION_ID}"
        val cancelUrl = "${PaypalReturnSchemas.CANCEL.schema}${BuildConfig.APPLICATION_ID}"
        payPalV2Repository.createToken(
          walletAddress = address,
          returnUrl = returnUrl,
          cancelUrl = cancelUrl
        )
      }
  }

}
