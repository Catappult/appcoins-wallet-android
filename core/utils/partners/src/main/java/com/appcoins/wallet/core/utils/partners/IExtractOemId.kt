package com.appcoins.wallet.core.utils.partners

import io.reactivex.Single

interface IExtractOemId {
  fun extract(packageName: String): Single<String>
}