package com.asfoundation.wallet.promotions

import io.reactivex.Single

class ReferralTestInteractor {

  fun hasReferralUpdate(): Single<Boolean> {
    return Single.just(true)
  }
}
