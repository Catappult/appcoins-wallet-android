package com.asfoundation.wallet.verification

interface VerificationActivityView {

  fun cancel()

  fun complete()

  fun lockRotation()

  fun unlockRotation()

  fun getCurrentFragment(): String

}
