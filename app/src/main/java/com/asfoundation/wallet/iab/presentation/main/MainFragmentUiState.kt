package com.asfoundation.wallet.iab.presentation.main

import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.payment_manager.PaymentMethod
import com.asfoundation.wallet.iab.presentation.BonusInfoData
import com.asfoundation.wallet.iab.presentation.PurchaseInfoData

sealed class MainFragmentUiState {
  data object LoadingDisclaimer : MainFragmentUiState()
  data object NoConnection : MainFragmentUiState()
  data object Error : MainFragmentUiState()
  data class Idle(
    val showDisclaimer: Boolean,
    val preSelectedPaymentMethod: PaymentMethod?,
    val bonusAvailable: Boolean,
    val purchaseData: PurchaseData,
    val purchaseInfoData: PurchaseInfoData,
    val bonusInfoData: BonusInfoData,
  ) : MainFragmentUiState()

  data class LoadingPurchaseData(
    val showDisclaimer: Boolean,
    val showPreSelectedPaymentMethod: Boolean
  ) : MainFragmentUiState()
}
