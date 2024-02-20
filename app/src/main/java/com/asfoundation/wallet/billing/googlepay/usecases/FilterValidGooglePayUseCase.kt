package com.asfoundation.wallet.billing.googlepay.usecases

import android.content.Context
import com.appcoins.wallet.ui.widgets.isPackageInstalled
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.ui.iab.PaymentMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FilterValidGooglePayUseCase @Inject constructor(
  @ApplicationContext val context: Context,
) {
  val CHOME_PACKAGE = "com.android.chrome"

  operator fun invoke(paymentMethods: List<PaymentMethod>): List<PaymentMethod> {
    return if (isPackageInstalled(CHOME_PACKAGE, context.packageManager)) {
      paymentMethods
    } else {
      paymentMethods
        .filter { it.id != PaymentType.GOOGLEPAY_WEB.subTypes[0] }
    }
  }

}
