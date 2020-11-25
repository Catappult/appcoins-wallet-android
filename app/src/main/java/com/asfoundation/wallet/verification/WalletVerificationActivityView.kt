package com.asfoundation.wallet.verification

interface WalletVerificationActivityView {

  fun cancel()

  fun lockRotation()

  fun unlockRotation()

  fun getCurrentFragment(): String

}
