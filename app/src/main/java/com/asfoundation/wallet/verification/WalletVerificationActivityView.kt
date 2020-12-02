package com.asfoundation.wallet.verification

interface WalletVerificationActivityView {

  fun cancel()

  fun complete()

  fun lockRotation()

  fun unlockRotation()

  fun getCurrentFragment(): String

}
