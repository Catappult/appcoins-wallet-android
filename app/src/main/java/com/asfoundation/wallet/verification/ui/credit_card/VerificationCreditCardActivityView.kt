package com.asfoundation.wallet.verification.ui.credit_card

import io.reactivex.Observable

interface VerificationCreditCardActivityView {

  fun cancel()

  fun complete()

  fun lockRotation()

  fun unlockRotation()

  fun getCurrentFragment(): String

  fun getToolbarBackPressEvents(): Observable<String>

  fun hideLoading()
}
