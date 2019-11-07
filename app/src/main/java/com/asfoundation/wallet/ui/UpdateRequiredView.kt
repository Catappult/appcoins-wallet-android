package com.asfoundation.wallet.ui

import io.reactivex.Observable

interface UpdateRequiredView {
  fun navigateToStoreAppView(deepLink: String)
  fun updateClick(): Observable<Any>
}
