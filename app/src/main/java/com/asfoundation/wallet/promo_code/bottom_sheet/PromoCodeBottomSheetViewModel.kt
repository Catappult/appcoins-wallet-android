package com.asfoundation.wallet.promo_code.bottom_sheet

import android.util.Log
import com.asfoundation.wallet.base.Async
import com.asfoundation.wallet.base.BaseViewModel
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.ViewState
import com.asfoundation.wallet.promo_code.repository.PromoCodeEntity
import com.asfoundation.wallet.promo_code.use_cases.DeletePromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.promo_code.use_cases.SetPromoCodeUseCase
import io.reactivex.Scheduler

sealed class PromoCodeBottomSheetSideEffect : SideEffect {
  object NavigateBack : PromoCodeBottomSheetSideEffect()
}

data class PromoCodeBottomSheetState(
    val promoCodeAsync: Async<PromoCodeEntity> = Async.Uninitialized,
    val submitClickAsync: Async<Unit> = Async.Uninitialized) : ViewState

class PromoCodeBottomSheetViewModel(private val networkScheduler: Scheduler,
                                    private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
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
    getCurrentPromoCodeUseCase()
        .asAsyncToState() {
          Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: getCurrentPromoCode: state $it")
          copy(promoCodeAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun submitClick(promoCodeString: String) {
    Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: submitClick: code typed: $promoCodeString")
    setPromoCodeUseCase(promoCodeString)
        .asAsyncToState() {
          Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: submitClick: state $it")
          copy(submitClickAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun replaceClick() {
    Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: replaceClick: ")
  }

  fun deleteClick() {
    Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: deleteClick: ")
    deletePromoCodeUseCase()
        .asAsyncToState() {
          Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: deleteClick: state $it")
          copy(promoCodeAsync = Async.Uninitialized)
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