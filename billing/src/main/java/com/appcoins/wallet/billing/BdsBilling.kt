package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import io.reactivex.Single

internal class BdsBilling(private val repository: Repository) : Billing {
  override fun isInAppSupported(packageName: String): Single<Boolean> {
    return repository.isSupported(packageName, BillingSupportedType.INAPP)
  }

  override fun isSubsSupported(packageName: String): Single<Boolean> {
    return repository.isSupported(packageName, BillingSupportedType.SUBS)
  }
}