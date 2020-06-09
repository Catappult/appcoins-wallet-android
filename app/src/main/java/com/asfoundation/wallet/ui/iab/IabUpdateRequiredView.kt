package com.asfoundation.wallet.ui.iab

import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable

interface IabUpdateRequiredView {

  fun navigateToIntent(intent: Intent)

  fun updateClick(): Observable<Any>

  fun cancelClick(): Observable<Any>

  fun close()
  fun showError(): Snackbar
}
