package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import com.asfoundation.wallet.billing.paypal.models.PaypalCreateAgreement
import io.reactivex.Single
import javax.inject.Inject

class CreatePaypalAgreementUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(token: String): Single<PaypalCreateAgreement> {
    return walletService.getWalletAddress()
      .flatMap { address ->
        payPalV2Repository.createBillingAgreement(
          walletAddress = address,
          token = token
        )
      }
  }

}
