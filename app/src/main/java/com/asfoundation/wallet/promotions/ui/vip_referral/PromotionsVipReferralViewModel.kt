package com.asfoundation.wallet.promotions.ui.vip_referral

import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.arch.BaseViewModel
import com.appcoins.wallet.core.arch.SideEffect
import com.appcoins.wallet.core.arch.ViewState
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.promotions.usecases.ConvertToLocalFiatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class PromotionsVipReferralState(
  val convertTotalAsync: Async<FiatValue> = Async.Uninitialized,
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
