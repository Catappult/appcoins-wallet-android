package com.appcoins.wallet.core.analytics.analytics.partners

import io.reactivex.Single

interface IExtractOemId {
  fun extract(packageName: String): Single<String>
}