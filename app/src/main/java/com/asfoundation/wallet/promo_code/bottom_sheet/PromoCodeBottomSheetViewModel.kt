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
import io.reactivex.Completable
import io.reactivex.Scheduler

sealed class PromoCodeBottomSheetSideEffect : SideEffect {
  object NavigateBack : PromoCodeBottomSheetSideEffect()
}

data class PromoCodeBottomSheetState(
    val promoCodeAsync: Async<PromoCodeEntity> = Async.Uninitialized,
    val submitClickAsync: Async<Unit> = Async.Uninitialized,
    val shouldShowDefault: Boolean = false) : ViewState

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
          copy(promoCodeAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun submitClick(promoCodeString: String) {
    setPromoCodeUseCase(promoCodeString)
        .asAsyncToState() {
          Log.d("APPC-2709",
              "PromoCodeBottomSheetViewModel: submitClick: code typed: $promoCodeString ---- state: $it")
          copy(submitClickAsync = it)
        }
        .scopedSubscribe() { e ->
          e.printStackTrace()
        }
  }

  fun replaceClick() {
    getCurrentPromoCodeUseCase()
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
          Log.d("APPC-2709", "PromoCodeBottomSheetViewModel: deleteClick: state $it")
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