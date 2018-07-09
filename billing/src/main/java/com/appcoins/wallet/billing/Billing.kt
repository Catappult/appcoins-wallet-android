package com.appcoins.wallet.billing

import io.reactivex.Single

internal interface Billing {
  fun isSubsSupported(packageName: String): Single<Boolean>
  fun isInAppSupported(packageName: String): Single<Boolean>
}