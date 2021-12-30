package com.asfoundation.wallet.ui.backup.skip

class SkipDialogNavigator(val fragment: SkipDialogFragment) {

  fun navigateBack() {
    fragment.dismiss()
  }

  fun finishBackup() {
    fragment.activity?.finish()
  }
}