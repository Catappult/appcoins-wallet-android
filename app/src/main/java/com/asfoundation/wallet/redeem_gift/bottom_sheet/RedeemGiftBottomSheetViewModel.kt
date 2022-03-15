package com.asfoundation.wallet.redeem_gift.bottom_sheet

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObserveCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class RedeemGiftBottomSheetSideEffect : SideEffect {
  object NavigateBack : RedeemGiftBottomSheetSideEffect()
}

data class RedeemGiftBottomSheetState(val promoCodeAsync: Async<PromoCode> = Async.Uninitialized,
                                     val submitClickAsync: Async<Unit> = Async.Uninitialized,
                                     val shouldShowDefault: Boolean = false) : ViewState

@HiltViewModel
class RedeemGiftBottomSheetViewModel @Inject constructor(
  private val observeCurrentPromoCodeUseCase: ObserveCurrentPromoCodeUseCase,
  private val setPromoCodeUseCase: SetPromoCodeUseCase,
  private val deletePromoCodeUseCase: DeletePromoCodeUseCase) :
  BaseViewModel<RedeemGiftBottomSheetState, RedeemGiftBottomSheetSideEffect>(initialState()) {

  companion object {
    fun initialState(): RedeemGiftBottomSheetState {
      return RedeemGiftBottomSheetState()
    }
  }

  init {
    getCurrentRedeemGift()
  }

  private fun getCurrentRedeemGift() {
    observeCurrentPromoCodeUseCase()
      .asAsyncToState { copy(promoCodeAsync = it) }
      .repeatableScopedSubscribe(RedeemGiftBottomSheetState::promoCodeAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun submitClick(redeemGiftString: String) {
    setPromoCodeUseCase(redeemGiftString)
      .asAsyncToState { copy(submitClickAsync = it) }
      .repeatableScopedSubscribe(RedeemGiftBottomSheetState::submitClickAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun replaceClick() = setState { copy(shouldShowDefault = true) }

  fun deleteClick() {
    deletePromoCodeUseCase()
      .asAsyncToState { copy(promoCodeAsync = Async.Uninitialized) }
      .doOnComplete {
        sendSideEffect {
          RedeemGiftBottomSheetSideEffect.NavigateBack
        }
      }
      .repeatableScopedSubscribe(
        RedeemGiftBottomSheetState::submitClickAsync.name + "_delete"
      ) { e ->
        e.printStackTrace()
      }
  }

  fun successGotItClick() = sendSideEffect { RedeemGiftBottomSheetSideEffect.NavigateBack }
}
