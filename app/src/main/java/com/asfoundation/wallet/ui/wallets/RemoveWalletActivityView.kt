package com.asfoundation.wallet.ui.wallets

interface RemoveWalletActivityView {
  fun navigateToWalletRemoveConfirmation()
  fun navigateToWalletList()
  fun navigateToBackUp(walletAddress: String)
  fun showRemoveWalletAnimation()
}
