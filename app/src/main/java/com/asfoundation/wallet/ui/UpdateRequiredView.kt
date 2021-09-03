package com.asfoundation.wallet.ui

import android.content.Intent
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable

interface UpdateRequiredView {

  fun navigateToIntent(intent: Intent)

  fun updateClick(): Observable<Any>

  fun showError(): Snackbar
}
