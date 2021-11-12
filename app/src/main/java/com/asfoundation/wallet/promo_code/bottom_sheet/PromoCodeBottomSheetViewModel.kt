package com.asfoundation.wallet.promo_code.bottom_sheet

import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObserveCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase

sealed class PromoCodeBottomSheetSideEffect : SideEffect {
  object NavigateBack : PromoCodeBottomSheetSideEffect()
}

data class PromoCodeBottomSheetState(
    val promoCodeAsync: Async<PromoCode> = Async.Uninitialized,
    val submitClickAsync: Async<Unit> = Async.Uninitialized,
    val shouldShowDefault: Boolean = false) : ViewState

class PromoCodeBottomSheetViewModel(
    private val observeCurrentPromoCodeUseCase: ObserveCurrentPromoCodeUseCase,
    private val setPromoCodeUseCase: SetPromoCodeUseCase,
    private val deletePromoCodeUseCase: DeletePromoCodeUseCase) :
    BaseViewModel<PromoCodeBottomSheetState, PromoCodeBottomSheetSideEffect>(
        initialState()) {

  companion object {
    fun initialState(): PromoCodeBottomSheetState {
      return PromoCodeBottomSheetState()
    }
  }

  init {
    getCurrentPromoCode()
  }

  private fun getCurrentPromoCode() {
    observeCurrentPromoCodeUseCase()
        .asAsyncToState() {
          copy(promoCodeAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun submitClick(promoCodeString: String) {
    setPromoCodeUseCase(promoCodeString)
        .asAsyncToState() {
          copy(submitClickAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun replaceClick() {
    observeCurrentPromoCodeUseCase()
        .asAsyncToState() {
          copy(shouldShowDefault = true)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun deleteClick() {
    deletePromoCodeUseCase()
        .asAsyncToState() {
          copy(promoCodeAsync = Async.Uninitialized)
        }
        .doOnComplete {
          sendSideEffect {
            PromoCodeBottomSheetSideEffect.NavigateBack
          }
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun successGotItClick() {
    sendSideEffect {
      PromoCodeBottomSheetSideEffect.NavigateBack
    }
  }
}