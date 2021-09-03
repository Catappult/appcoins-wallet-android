package com.asfoundation.wallet.ui.wallets

import io.reactivex.Observable

interface RemoveWalletActivityView {

  fun navigateToWalletRemoveConfirmation()

  fun navigateToWalletList()

  fun navigateToBackUp(walletAddress: String)

  fun showRemoveWalletAnimation()

  fun showAuthentication()

  fun authenticationResult(): Observable<Boolean>
}
