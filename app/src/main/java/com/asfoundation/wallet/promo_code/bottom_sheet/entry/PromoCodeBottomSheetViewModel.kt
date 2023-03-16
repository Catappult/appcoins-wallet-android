package com.asfoundation.wallet.promo_code.bottom_sheet.entry

import com.appcoins.wallet.ui.arch.Async
import com.appcoins.wallet.ui.arch.BaseViewModel
import com.appcoins.wallet.ui.arch.SideEffect
import com.appcoins.wallet.ui.arch.ViewState
import com.asfoundation.wallet.promo_code.FailedPromoCode
import com.asfoundation.wallet.promo_code.PromoCodeResult
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetStoredPromoCodeResultUseCase
import com.asfoundation.wallet.promo_code.use_cases.VerifyAndSavePromoCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class PromoCodeBottomSheetSideEffect : SideEffect {
  object NavigateBack : PromoCodeBottomSheetSideEffect()
}

data class PromoCodeBottomSheetState(
  val storedPromoCodeAsync: Async<PromoCodeResult> = Async.Uninitialized,
  val submitPromoCodeAsync: Async<PromoCodeResult> = Async.Uninitialized,
  val shouldShowDefault: Boolean = false
) : ViewState

@HiltViewModel
class PromoCodeBottomSheetViewModel @Inject constructor(
  private val getStoredPromoCodeResultUseCase: GetStoredPromoCodeResultUseCase,
  private val verifyAndSavePromoCodeUseCase: VerifyAndSavePromoCodeUseCase,
  private val deletePromoCodeUseCase: DeletePromoCodeUseCase
) :
  BaseViewModel<PromoCodeBottomSheetState, PromoCodeBottomSheetSideEffect>(initialState()) {

  // to prevent success being called multiple times when any async is changed
  var isFirstSuccess = true

  companion object {
    fun initialState(): PromoCodeBottomSheetState {
      return PromoCodeBottomSheetState()
    }
  }

  init {
    getCurrentPromoCode()
  }

  private fun getCurrentPromoCode() {
    getStoredPromoCodeResultUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = it) }
      .doOnNext {
        if (it == FailedPromoCode.ExpiredCode()) deleteCode()
      }
      .repeatableScopedSubscribe(PromoCodeBottomSheetState::storedPromoCodeAsync.name) { e ->
        e.printStackTrace()
      }
  }

  fun submitClick(promoCodeString: String) {
    verifyAndSavePromoCodeUseCase(promoCodeString)
      .asAsyncToState { copy(submitPromoCodeAsync = it) }
      .scopedSubscribe()
  }

  fun replaceClick() = setState { copy(shouldShowDefault = true) }

  fun deleteClick() {
    deletePromoCodeUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = Async.Uninitialized) }
      .doOnComplete {
        sendSideEffect {
          PromoCodeBottomSheetSideEffect.NavigateBack
        }
      }
      .scopedSubscribe()
  }

  private fun deleteCode() {
    deletePromoCodeUseCase()
      .asAsyncToState { copy(storedPromoCodeAsync = Async.Uninitialized) }
      .scopedSubscribe()
  }
}