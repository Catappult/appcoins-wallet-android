package com.appcoins.wallet.billing.adyen

import androidx.fragment.app.Fragment
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.util.Error
import java.math.BigDecimal

data class PaymentInfoModel(
  val paymentMethod: ModelObject?,
  val isStored: Boolean = false,
  val priceAmount: BigDecimal,
  val priceCurrency: String,
  val cardComponent: ((Fragment, CardConfiguration) -> CardComponent)? = null,
  val supportedShopperInteractions: List<String> = emptyList(),
  val error: Error = Error()
) {

  constructor(error: Error) : this(null, false, BigDecimal.ZERO, "", null, emptyList(), error)

  constructor(paymentMethod: PaymentMethod, value: BigDecimal, currency: String) : this(
    paymentMethod,
    false,
    value,
    currency,
    { fragment: Fragment, config: CardConfiguration ->
      CardComponent.PROVIDER.get(fragment, paymentMethod, config)
    }
  )

  constructor(paymentMethod: StoredPaymentMethod, value: BigDecimal, currency: String) : this(
    paymentMethod,
    true,
    value,
    currency,
    { fragment: Fragment, config: CardConfiguration ->
      CardComponent.PROVIDER.get(fragment, paymentMethod, config)
    },
    paymentMethod.supportedShopperInteractions ?: emptyList()
  )
}
