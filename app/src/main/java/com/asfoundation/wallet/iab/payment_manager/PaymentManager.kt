package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.asfoundation.wallet.iab.domain.use_case.GetPaymentMethodsUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethodsInteractor
import javax.inject.Inject

class PaymentManager @Inject constructor(
  private val paymentMethodsInteractor: PaymentMethodsInteractor,
  private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
) {

  private var paymentMethods: List<PaymentMethodEntity>? = null

  private var selectedPaymentMethod: PaymentMethodEntity? = null

  suspend fun getPaymentMethods() =
    paymentMethods.takeIf { it != null }
      ?: getPaymentMethodsUseCase()
        .also { paymentMethods = it }

  fun hasPreSelectedPaymentMethod() = paymentMethodsInteractor.hasPreSelectedPaymentMethod()

  fun setSelectedPaymentMethod(id: String) {
    selectedPaymentMethod = paymentMethods?.first { it.id == id }
  }

  suspend fun getSelectedPaymentMethod(): PaymentMethodEntity? {
    val id = paymentMethodsInteractor.getLastUsedPaymentMethodV2()

    if (id.isNullOrEmpty()) return null

    getPaymentMethods()

    return paymentMethods?.firstOrNull { it.id == id }
  }
}