package com.asfoundation.wallet.recover.entry

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.onboarding.OnboardingFragmentDirections
import javax.inject.Inject

class RecoverEntryNavigator @Inject constructor(val fragment: Fragment) : Navigator{

  fun launchFileIntent(storageIntentLauncher: ActivityResultLauncher<Intent>,
                       path: Uri?) {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "*/*"
      path?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) putExtra(
            DocumentsContract.EXTRA_INITIAL_URI, it)
      }
    }
    return try {
      storageIntentLauncher.launch(intent)
    } catch (e: ActivityNotFoundException) {
    }
  }

  fun navigateToRecoverPasswordFragment(keystore: String, walletBalance : String, walletAddress : String){
    navigate(fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateToRecoverPassword(keystore, walletBalance, walletAddress)
    )
  }

  fun navigateToCreateWalletDialog(){
    navigate(fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateCreateWalletDialog()
    )
  }

  fun navigateBack() {
    fragment.requireActivity().finish()
  }

  fun navigateToMainActivity(fromSupportNotification: Boolean) {
    navigate(
      fragment.findNavController(),
      OnboardingFragmentDirections.actionNavigateToMainActivity(fromSupportNotification)
    )
  }
}