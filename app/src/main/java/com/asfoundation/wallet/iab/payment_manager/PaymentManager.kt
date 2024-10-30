package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.use_case.GetBalanceUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetPaymentMethodsUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetProductInfoUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetWalletInfoUseCase
import com.asfoundation.wallet.iab.payment_manager.domain.PaymentMethodInfo
import com.asfoundation.wallet.ui.iab.PaymentMethodsInteractor
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class PaymentManager @AssistedInject constructor(
  @Assisted private val purchaseData: PurchaseData,
  private val paymentMethodsInteractor: PaymentMethodsInteractor,
  private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase,
  private val getProductInfoUseCase: GetProductInfoUseCase,
  private val getCurrentWalletInfoUseCase: GetWalletInfoUseCase,
  private val getBalanceUseCase: GetBalanceUseCase,
  private val currencyFormatUtils: CurrencyFormatUtils,
) {

  private var paymentMethods: List<PaymentMethodInfo>? = null

  private var productInfo: ProductInfoData? = null

  val selectedPaymentMethod = MutableStateFlow<PaymentMethodInfo?>(null)

  suspend fun getProductInfo() = coroutineScope {
    if (productInfo != null) return@coroutineScope productInfo

    return@coroutineScope getProductInfoUseCase(
      packageName = purchaseData.domain,
      skuId = purchaseData.skuId ?: ""
    )
      .also { productInfo = it }
  }

  suspend fun getPaymentMethods(
    currencyType: String? = null,
    direct: Boolean? = null,
  ) = coroutineScope {
    paymentMethods?.let { return@coroutineScope it }

    val productInfoRequest = async { getProductInfo() }
    val walletRequest = async { getCurrentWalletInfoUseCase() }
    val walletInfoRequest = async { getBalanceUseCase() }

    val productInfo = productInfoRequest.await()
    val wallet = walletRequest.await()
    val walletInfo = walletInfoRequest.await()

    val paymentMethodsRequest = async {
      getPaymentMethodsUseCase(
        value = productInfo?.transaction?.amount.toString(),
        currency = productInfo?.transaction?.currency,
        currencyType = currencyType,
        direct = direct,
        transactionType = purchaseData.type,
        packageName = purchaseData.domain,
        entityOemId = purchaseData.oemId,
        address = wallet,
      )
    }

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

  fun hasPreSelectedPaymentMethod() =
    selectedPaymentMethod.value != null || paymentMethodsInteractor.hasPreSelectedPaymentMethod()

  fun setSelectedPaymentMethod(id: String) {
    selectedPaymentMethod.tryEmit(paymentMethods?.first { it.paymentMethod.id == id })
  }

  suspend fun getSelectedPaymentMethod(): PaymentMethodInfo? {
    val id = selectedPaymentMethod.value?.paymentMethod?.id
      ?: paymentMethodsInteractor.getLastUsedPaymentMethodV2()

    if (id.isNullOrEmpty()) return null

    getPaymentMethods()

    setSelectedPaymentMethod(id)

    return paymentMethods?.firstOrNull { it.paymentMethod.id == id }
  }
}
