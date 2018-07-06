package com.appcoins.wallet.billing

import io.reactivex.Single

internal interface Repository {
  fun isSupported(): Single<Boolean>
}
