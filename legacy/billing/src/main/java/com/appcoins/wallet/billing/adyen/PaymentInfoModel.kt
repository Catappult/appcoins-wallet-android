package com.appcoins.wallet.billing.adyen

import androidx.activity.ComponentActivity
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.util.Error
import java.math.BigDecimal

data class PaymentInfoModel(
  val paymentMethod: ModelObject?,
  var isStored: Boolean = false,
  val priceAmount: BigDecimal,
  val priceCurrency: String,
  var cardComponent: ((ComponentActivity, CardConfiguration) -> CardComponent)? = null,
  val supportedShopperInteractions: List<String> = emptyList(),
  val error: Error = Error()
) {

  constructor(error: Error) : this(null, false, BigDecimal.ZERO, "", null, emptyList(), error)

  constructor(paymentMethod: PaymentMethod, value: BigDecimal, currency: String) : this(
    paymentMethod,
    false,
    value,
    currency,
    { activity: ComponentActivity, config: CardConfiguration ->
      CardComponent.PROVIDER.get(activity, paymentMethod, config)
    }
  )

  constructor(paymentMethod: StoredPaymentMethod, value: BigDecimal, currency: String) : this(
    paymentMethod,
    true,
    value,
    currency,
    { activity: ComponentActivity, config: CardConfiguration ->
      CardComponent.PROVIDER.get(activity, paymentMethod, config)
    },
    paymentMethod.supportedShopperInteractions ?: emptyList()
  )
}
