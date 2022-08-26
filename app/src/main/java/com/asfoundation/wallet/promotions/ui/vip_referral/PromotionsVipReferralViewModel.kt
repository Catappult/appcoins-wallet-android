package com.asfoundation.wallet.promotions.ui.vip_referral

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.promotions.usecases.ConvertToLocalFiatUseCase
import com.asfoundation.wallet.ui.iab.FiatValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


data class PromotionsVipReferralState(val convertTotalAsync: Async<FiatValue> = Async.Uninitialized,
                                      val shouldShowDefault: Boolean = false) : ViewState

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

//  fun successGotItClick() = sendSideEffect { PromotionsVipReferralSideEffect.NavigateBack }

}
