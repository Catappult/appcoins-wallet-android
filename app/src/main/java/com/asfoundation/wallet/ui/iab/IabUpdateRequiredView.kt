package com.asfoundation.wallet.ui.iab

import io.reactivex.Observable

interface IabUpdateRequiredView {
  fun navigateToStoreAppView(url: String)
  fun showError()
  fun updateClick(): Observable<Any>
  fun cancelClick(): Observable<Any>
  fun close()
}
