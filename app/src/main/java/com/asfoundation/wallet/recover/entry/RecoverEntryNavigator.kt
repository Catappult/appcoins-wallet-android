package com.asfoundation.wallet.recover.entry

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.ui.arch.data.Navigator
import com.appcoins.wallet.ui.arch.data.navigate
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.recover.RecoverActivity
import javax.inject.Inject

class RecoverEntryNavigator @Inject constructor(val fragment: Fragment) :
  Navigator {

  fun launchFileIntent(
    storageIntentLauncher: ActivityResultLauncher<Intent>,
    path: Uri?
  ) {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "*/*"
      path?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) putExtra(
          DocumentsContract.EXTRA_INITIAL_URI, it
        )
      }
    }
    return try {
      storageIntentLauncher.launch(intent)
    } catch (e: ActivityNotFoundException) {
    }
  }

  fun navigateToRecoverPasswordFragment(
    keystore: WalletKeyStore,
    walletBalance: String,
    walletAddress: String,
    isFromOnboarding: Boolean
  ) {
    navigate(
      fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateToRecoverPassword(
        keystore,
        walletBalance,
        walletAddress, isFromOnboarding
      )
    )
  }

  fun navigateToCreateWalletDialog(isFromOnboarding: Boolean) {
    navigate(
      fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateCreateWalletDialog(
        needsWalletCreation = false,
        isFromOnboarding = isFromOnboarding
      )
    )
  }

  fun navigateBack(fromActivity: Boolean) {
    if (fromActivity) {
      fragment.requireActivity().finish()
    } else {
      fragment.requireActivity().onBackPressed()
    }
  }

  fun navigateToNavigationBar() {
    /* Temporary workaround for the RecoverActivity */
    fragment.requireActivity()
      .takeIf { it is RecoverActivity }?.finish()
      ?: navigate(
        fragment.findNavController(),
        RecoverEntryFragmentDirections.actionNavigateToNavBarFragment()
      )
  }
}