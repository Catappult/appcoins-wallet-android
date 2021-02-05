package com.asfoundation.wallet.verification

import io.reactivex.Observable

interface VerificationActivityView {

  fun cancel()

  fun complete()

  fun lockRotation()

  fun unlockRotation()

  fun getCurrentFragment(): String

  fun getToolbarBackPressEvents(): Observable<String>

  fun hideLoading()
}
