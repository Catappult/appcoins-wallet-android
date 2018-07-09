package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import io.reactivex.Single

internal interface Repository {
  fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean>
}
