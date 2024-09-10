package com.asfoundation.wallet.billing.paypal.usecases

import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import javax.inject.Inject

class GetPayPalResultUseCase @Inject constructor(
  private val payPalV2Repository: PayPalV2Repository,
) {

  operator fun invoke(): String {
    return payPalV2Repository.consumeChromeResult()
  }

}
