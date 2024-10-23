package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.use_case.GetBalanceUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetPaymentMethodsUseCase
import com.asfoundation.wallet.iab.payment_manager.domain.PaymentMethodInfo
import com.asfoundation.wallet.ui.iab.PaymentMethodsInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class PaymentManager @Inject constructor(
  private val paymentMethodsInteractor: PaymentMethodsInteractor,
  private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
  private val getBalanceUseCase: GetBalanceUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
) {

  private var paymentMethods: List<PaymentMethodInfo>? = null

  val selectedPaymentMethod = MutableStateFlow<PaymentMethodInfo?>(null)

  suspend fun getPaymentMethods(
    value: String? = null,
    currency: String? = null,
    currencyType: String? = null,
    direct: Boolean? = null,
    transactionType: String? = null,
    packageName: String? = null,
    entityOemId: String? = null,
    address: String? = null,
  ) = coroutineScope {
    paymentMethods?.let { return@coroutineScope it }

    val walletInfoRequest = async { getBalanceUseCase() }
    val paymentMethodsRequest = async {
      getPaymentMethodsUseCase(
        value = value,
        currency = currency,
        currencyType = currencyType,
        direct = direct,
        transactionType = transactionType,
        packageName = packageName,
        entityOemId = entityOemId,
        address = address,
      )
    }

    val walletInfo = walletInfoRequest.await()
    val paymentMethods = paymentMethodsRequest.await()

    return@coroutineScope paymentMethods.map { paymentMethod ->
      PaymentMethodInfo(
        paymentMethod = paymentMethod,
        balance = when (paymentMethod.id) {
          PaymentMethodEntity.CREDITS_ID -> {
            walletInfo.walletBalance.creditsBalance.fiat.run {
              "$symbol ${currencyFormatUtils.formatCurrency(amount)}"
            }
          }

          else -> null
        }
      )
    }.also { this@PaymentManager.paymentMethods = it }
  }

  fun hasPreSelectedPaymentMethod() = paymentMethodsInteractor.hasPreSelectedPaymentMethod()

  fun setSelectedPaymentMethod(id: String) {
    selectedPaymentMethod.tryEmit(paymentMethods?.first { it.paymentMethod.id == id })
  }

  suspend fun getSelectedPaymentMethod(): PaymentMethodInfo? {
    val id = paymentMethodsInteractor.getLastUsedPaymentMethodV2()

    if (id.isNullOrEmpty()) return null

    getPaymentMethods()

    setSelectedPaymentMethod(id)

    return paymentMethods?.firstOrNull { it.paymentMethod.id == id }
  }
}
