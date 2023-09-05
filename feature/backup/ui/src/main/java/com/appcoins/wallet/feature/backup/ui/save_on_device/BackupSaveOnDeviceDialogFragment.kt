package com.appcoins.wallet.feature.backup.ui.save_on_device

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.feature.backup.ui.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSaveOnDeviceDialogFragment :
  BottomSheetDialogFragment(),
  SingleStateFragment<BackupSaveOnDeviceDialogState, BackupSaveOnDeviceDialogSideEffect> {

  @Inject
  lateinit var navigator: BackupSaveOnDeviceDialogNavigator

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val PASSWORD_KEY = "password"

    @JvmStatic
    fun newInstance(walletAddress: String, password: String) =
      BackupSaveOnDeviceDialogFragment().apply {
        arguments =
          Bundle().apply {
            putString(WALLET_ADDRESS_KEY, walletAddress)
            putString(PASSWORD_KEY, password)
          }
      }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int = R.style.AppBottomSheetDialogThemeDraggable

  override fun onSideEffect(sideEffect: BackupSaveOnDeviceDialogSideEffect) =
    when (sideEffect) {
      is BackupSaveOnDeviceDialogSideEffect.NavigateToSuccess ->
        navigator.navigateToSuccessScreen(sideEffect.walletAddress)

      BackupSaveOnDeviceDialogSideEffect.ShowError -> showError()
    }

  fun showError() {
    Toast.makeText(context, R.string.error_export, Toast.LENGTH_LONG).show()
    requireActivity().finish()
  }

  override fun onStateChanged(state: BackupSaveOnDeviceDialogState) {
    // Do nothing
  }
}
