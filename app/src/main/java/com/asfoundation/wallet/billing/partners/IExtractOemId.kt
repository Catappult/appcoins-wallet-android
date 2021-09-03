package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

interface IExtractOemId {
  fun extract(packageName: String): Single<String>
}