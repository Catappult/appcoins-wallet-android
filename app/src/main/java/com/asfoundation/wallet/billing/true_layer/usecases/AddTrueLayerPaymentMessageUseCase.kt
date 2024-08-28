package com.asfoundation.wallet.billing.true_layer.usecases

import android.content.Context
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.ui.iab.PaymentMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AddTrueLayerPaymentMessageUseCase @Inject constructor(
  @ApplicationContext val context: Context,
) {

  operator fun invoke(paymentMethods: List<PaymentMethod>): List<PaymentMethod> {
    return paymentMethods.map { method ->
      if (method.id == PaymentType.TRUE_LAYER.subTypes[0])
        method.copy(
          message = context.resources.getString(R.string.purchase_bank_transer_secure_body)
        )
      else
        method
    }
  }

}
