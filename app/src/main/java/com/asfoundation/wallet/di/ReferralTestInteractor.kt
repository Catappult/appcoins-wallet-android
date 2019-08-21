package com.asfoundation.wallet.di

import io.reactivex.Single

//TODO Remove when real implementation is created
class ReferralTestInteractor {

  fun hasReferralUpdate(): Single<Boolean> {
    return Single.just(false)
  }
}
