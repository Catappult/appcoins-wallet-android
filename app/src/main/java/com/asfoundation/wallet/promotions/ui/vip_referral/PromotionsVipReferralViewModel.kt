package com.asfoundation.wallet.promotions.ui.vip_referral

import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.promotions.usecases.ConvertToLocalFiatUseCase
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class PromotionsVipReferralState(
  val convertTotalAsync: Async<com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue> = Async.Uninitialized,
  val shouldShowDefault: Boolean = false
) : ViewState

sealed class PromotionsVipReferralSideEffect : SideEffect {
  object NavigateBack : PromotionsVipReferralSideEffect()
}

@HiltViewModel
class PromotionsVipReferralViewModel @Inject constructor(
  private val convertToLocalFiatUseCase: ConvertToLocalFiatUseCase
) :
  BaseViewModel<PromotionsVipReferralState, PromotionsVipReferralSideEffect>(initialState()) {

  companion object {
    fun initialState(): PromotionsVipReferralState {
      return PromotionsVipReferralState()
    }
  }

  fun getCurrency(earnedValue: String) {
    convertToLocalFiatUseCase(earnedValue, "USD")
      .asAsyncToState { async ->
        copy(convertTotalAsync = async)
      }
      .repeatableScopedSubscribe(PromotionsVipReferralState::convertTotalAsync.name) { e ->
        e.printStackTrace()
      }
  }

}
