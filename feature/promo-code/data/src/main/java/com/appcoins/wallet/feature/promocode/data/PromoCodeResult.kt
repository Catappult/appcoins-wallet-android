package com.appcoins.wallet.feature.promocode.data

import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.repository.ValidityState

sealed class PromoCodeResult

data class SuccessfulPromoCode(val promoCode: PromoCode) : PromoCodeResult()

sealed class FailedPromoCode : PromoCodeResult() {
  data class GenericError(val throwable: Throwable? = null) : FailedPromoCode()
  data class InvalidCode(val throwable: Throwable? = null) : FailedPromoCode()
  data class ExpiredCode(val throwable: Throwable? = null) : FailedPromoCode()
  data class CodeNotAdded(val throwable: Throwable? = null) : FailedPromoCode()
}

class PromoCodeMapper {
  fun map(promoCode: PromoCode): PromoCodeResult {
    return when (promoCode.validity) {
      ValidityState.ACTIVE -> SuccessfulPromoCode(promoCode)
      ValidityState.EXPIRED -> FailedPromoCode.ExpiredCode()
      ValidityState.ERROR -> FailedPromoCode.InvalidCode()
      ValidityState.NOT_ADDED -> FailedPromoCode.CodeNotAdded()
      else -> FailedPromoCode.CodeNotAdded()
    }
  }
}
