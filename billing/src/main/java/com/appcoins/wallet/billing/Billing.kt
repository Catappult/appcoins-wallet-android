package com.appcoins.wallet.billing

import io.reactivex.Single

interface Billing {
  fun isSupported(): Single<Boolean>
}