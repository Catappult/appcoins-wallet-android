package com.asfoundation.wallet.promo_code.bottom_sheet

import com.asfoundation.wallet.promo_code.repository.PromoCode
import io.reactivex.Observable

sealed class PromoCodeResult

data class SuccessfulPromoCode(val promoCode: PromoCode) : PromoCodeResult()

sealed class FailedPromoCode : PromoCodeResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedPromoCode()
  data class InvalidCode(val throwable: Throwable? = null) : FailedPromoCode()
  data class ExpiredCode(val throwable: Throwable? = null) : FailedPromoCode()
  data class CodeNotAdded(val throwable: Throwable? = null) : FailedPromoCode()
}

class PromoCodeMapper {
  fun map(promoCode: PromoCode): Observable<PromoCodeResult> {
    return when (promoCode.expired) {
      false -> {
        (Observable.just(SuccessfulPromoCode(promoCode)))
      }
      true -> {
        Observable.just(FailedPromoCode.ExpiredCode())
      }
      else -> {
        Observable.just(FailedPromoCode.CodeNotAdded())
      }
    }
  }
}
