package com.asfoundation.wallet.my_wallets.create_wallet

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import javax.inject.Inject

class CreateWalletDialogNavigator @Inject constructor(private val fragment: Fragment) {

  fun navigateBack() {
    (fragment as DialogFragment).dismiss()
  }
}