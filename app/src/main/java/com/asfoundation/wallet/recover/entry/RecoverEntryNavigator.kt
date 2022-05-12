package com.asfoundation.wallet.recover.entry

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.ActivityNavigator
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class RecoverEntryNavigator @Inject constructor(val fragment: Fragment) : Navigator {

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
    keystore: String,
    walletBalance: String,
    walletAddress: String
  ) {
    navigate(
      fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateToRecoverPassword(
        keystore,
        walletBalance,
        walletAddress
      )
    )
  }

  fun navigateToCreateWalletDialog() {
    navigate(
      fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateCreateWalletDialog(needsWalletCreation = false)
    )
  }

  fun navigateBack() {
    fragment.requireActivity().finish()
  }

  //when navigation component doesn't have this limitation anymore, this extras should be removed and this should work with popUpTo
  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    val clearBackStackExtras = ActivityNavigator.Extras.Builder()
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .build()

    navigate(
      fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateToMainActivity(fromSupportNotification),
      extras = clearBackStackExtras
    )
  }
}