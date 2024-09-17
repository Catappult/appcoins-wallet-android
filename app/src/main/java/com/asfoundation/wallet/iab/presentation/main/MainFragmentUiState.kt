package com.asfoundation.wallet.iab.presentation.main

import com.asfoundation.wallet.iab.presentation.BonusInfoData
import com.asfoundation.wallet.iab.presentation.PaymentMethodData
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData


sealed class MainFragmentUiState {
  object LoadingDisclaimer : MainFragmentUiState()
  object NoConnection : MainFragmentUiState()
  object Error : MainFragmentUiState()
  data class Idle(
    val showDisclaimer: Boolean,
    val showPreSelectedPaymentMethod: Boolean,
    val preSelectedPaymentMethodEnabled: Boolean,
    val bonusAvailable: Boolean,
    val purchaseInfoData: PurchaseInfoData,
    val bonusInfoData: BonusInfoData,
    val paymentMethodData: PaymentMethodData,
  ) : MainFragmentUiState()
  data class LoadingPurchaseData(
    val showDisclaimer: Boolean,
    val showPreSelectedPaymentMethod: Boolean
  ) : MainFragmentUiState()
}
