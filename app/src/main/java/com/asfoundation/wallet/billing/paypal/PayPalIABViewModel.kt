package com.asfoundation.wallet.billing.paypal

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.promotions.usecases.ConvertToLocalFiatUseCase
import com.asfoundation.wallet.ui.iab.FiatValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class PayPalIABState(
  val convertTotalAsync: Async<FiatValue> = Async.Uninitialized,
  val shouldShowDefault: Boolean = false
) : ViewState

sealed class PayPalIABSideEffect : SideEffect {
  object NavigateBack : PayPalIABSideEffect()
}

@HiltViewModel
class PayPalIABViewModel @Inject constructor(
  private val convertToLocalFiatUseCase: ConvertToLocalFiatUseCase

) :
  BaseViewModel<PayPalIABState, PayPalIABSideEffect>(initialState()) {

  companion object {
    fun initialState(): PayPalIABState {
      return PayPalIABState()
    }
  }

  fun getCurrency(earnedValue: String) {
    convertToLocalFiatUseCase(earnedValue, "USD")
      .asAsyncToState { async ->
        copy(convertTotalAsync = async)
      }
      .repeatableScopedSubscribe(PayPalIABState::convertTotalAsync.name) { e ->
        e.printStackTrace()
      }
  }

}
