package com.asfoundation.wallet.iab.payment_manager

import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.use_case.GetBalanceUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetPaymentMethodsUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetProductInfoUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetWalletInfoUseCase
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
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
  private val ewtAuthenticatorService: EwtAuthenticatorService,
  private val paymentMethodFactories: PaymentMethodCreator
) {

  private var paymentMethods: List<PaymentMethod>? = null

  private var productInfo: ProductInfoData? = null

  private var walletData: WalletData? = null

  val selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)

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

    val productInfo = productInfoRequest.await() ?: throw RuntimeException("Error fetching sku details")
    val wallet = walletRequest.await()
    val walletInfo = walletInfoRequest.await()
    val walletEwt = ewtAuthenticatorService.getEwtAuthentication(wallet)

    walletData = WalletData(
      address = wallet,
      ewt = walletEwt,
      walletInfo = walletInfo
    )

    val paymentMethodsRequest = async {
      getPaymentMethodsUseCase(
        value = productInfo.transaction.amount.toString(),
        currency = productInfo.transaction.currency,
        currencyType = currencyType,
        direct = direct,
        transactionType = purchaseData.type,
        packageName = purchaseData.domain,
        entityOemId = purchaseData.oemId,
        address = wallet,
      )
    }

    paymentMethods = paymentMethodsRequest
      .await()
      .mapNotNull { paymentMethod ->
        paymentMethodFactories.create(
          paymentMethod = paymentMethod,
          purchaseData = purchaseData,
          walletData = walletData!!,
          productInfoData = productInfo
        )
      }

    return@coroutineScope paymentMethods
  }

  fun hasPreSelectedPaymentMethod() =
    selectedPaymentMethod.value != null || paymentMethodsInteractor.hasPreSelectedPaymentMethod()

  fun setSelectedPaymentMethod(id: String) {
    selectedPaymentMethod.tryEmit(paymentMethods?.first { it.id == id })
  }

  suspend fun getSelectedPaymentMethod(): PaymentMethod? {
    val id = selectedPaymentMethod.value?.id
      ?: paymentMethodsInteractor.getLastUsedPaymentMethodV2()

    if (id.isNullOrEmpty()) return null

    getPaymentMethods()

    setSelectedPaymentMethod(id)

    return paymentMethods?.firstOrNull { it.id == id }
  }
}
