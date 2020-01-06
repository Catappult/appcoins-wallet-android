package com.asfoundation.wallet.ui.balance

interface ImportWalletActivityView {
  fun navigateToPasswordView(keystore: String)
  fun showWalletImportAnimation()
  fun showWalletImportedAnimation()
  fun launchFileIntent()
  fun hideAnimation()

}
