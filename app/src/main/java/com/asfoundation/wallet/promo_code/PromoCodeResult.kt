package com.asfoundation.wallet.promo_code

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.ValidityState
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
    return when (promoCode.validity) {
      ValidityState.ACTIVE -> Observable.just(SuccessfulPromoCode(promoCode))
      ValidityState.EXPIRED -> Observable.just(FailedPromoCode.ExpiredCode())
      ValidityState.ERROR -> Observable.just(FailedPromoCode.InvalidCode())
      ValidityState.NOT_ADDED -> Observable.just(FailedPromoCode.CodeNotAdded())
      else -> Observable.just(FailedPromoCode.CodeNotAdded())
    }
  }
}
