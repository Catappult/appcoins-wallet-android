package com.asfoundation.wallet.iab.payment_manager.factory

import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.domain.model.ProductInfoData
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.domain.use_case.CreateTransactionUseCase
import com.asfoundation.wallet.iab.domain.use_case.GetStoredCardsV2UseCase
import com.asfoundation.wallet.iab.domain.use_case.LoadPaymentInfoUseCase
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.payment_manager.PaymentMethodFactory
import com.asfoundation.wallet.iab.payment_manager.domain.WalletData
import com.asfoundation.wallet.iab.payment_manager.payment_methods.CreditCardPaymentMethod
import javax.inject.Inject

class CreditCardFactory @Inject constructor(
  private val loadPaymentInfoUseCase: LoadPaymentInfoUseCase,
  private val getStoredCardsUseCase: GetStoredCardsV2UseCase,
  private val createTransactionUseCase: CreateTransactionUseCase,
) : PaymentMethodFactory {

  companion object {
    private const val ID = "credit_card"
  }

  override fun create(
    paymentMethodEntity: PaymentMethodEntity,
    purchaseData: PurchaseData,
    productInfoData: ProductInfoData,
    walletData: WalletData,
    currencyFormatUtils: CurrencyFormatUtils
  ): PaymentMethod? {
    if (paymentMethodEntity.id != ID) return null

    return CreditCardPaymentMethod(
      paymentMethod = paymentMethodEntity,
      purchaseData = purchaseData,
      walletData = walletData,
      loadPaymentInfoUseCase = loadPaymentInfoUseCase,
      getStoredCardsUseCase = getStoredCardsUseCase,
      createTransactionUseCase = createTransactionUseCase,
    )
  }
}
