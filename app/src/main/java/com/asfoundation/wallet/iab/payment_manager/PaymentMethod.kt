package com.asfoundation.wallet.iab.payment_manager

import androidx.annotation.StringRes
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asf.wallet.R
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.iab.payment_manager.domain.Transaction
import com.asfoundation.wallet.iab.payment_manager.domain.TransactionData
import java.math.BigDecimal
import java.util.Currency

abstract class PaymentMethod(protected val paymentMethod: PaymentMethodEntity) {

  open val id: String
    get() = paymentMethod.id
  open val name: String
    get() = paymentMethod.label
  open val icon: String
    get() = paymentMethod.iconUrl
  open val isEnable: Boolean
    get() = paymentMethod.isAvailable()
  open val cost: BigDecimal
    get() = paymentMethod.price.value + (paymentMethod.fee?.cost?.value ?: BigDecimal.ZERO)
  open val fees: BigDecimal?
    get() = paymentMethod.fee?.cost?.value.takeIf { hasFees }
  open val subtotal: BigDecimal?
    get() = paymentMethod.fee?.run { paymentMethod.price.value }.takeIf { hasFees }
  open val currency: String
    get() = paymentMethod.price.currency
  open val currencySymbol: String
    get() = Currency.getInstance(currency).symbol
  open val hasFees: Boolean
    get() = paymentMethod.fee != null
  open val isReadyToPay: Boolean
    get() = true

  @get:StringRes
  open val onPreSelectedButtonLabel: Int
    get() = R.string.buy_button

  open fun getIconOnPreSelected(): String = icon

  open fun getTitleOnPreSelected(): String = name

  open fun getDescription(): String? =
    if (hasFees) "Fees and vat may apply" else paymentMethod.message // TODO hardcode text

  open val onPaymentMethodClick: (FragmentNavigator) -> Unit = { navigator ->
    navigator.navigateUp()
  }

  open suspend fun init() {
    return
  }

  abstract suspend fun createTransaction(transaction: TransactionData): Transaction
}
