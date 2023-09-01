package com.asfoundation.wallet.recover.entry

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.asfoundation.wallet.entity.WalletKeyStore
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletNameBottomSheetFragment
import com.asfoundation.wallet.promo_code.bottom_sheet.success.PromoCodeSuccessBottomSheetFragment
import com.asfoundation.wallet.recover.RecoverActivity
import com.asfoundation.wallet.recover.success.RecoveryWalletSuccessBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
    walletName: String?,
    isFromOnboarding: Boolean
  ) {
    navigate(
      fragment.findNavController(),
      RecoverEntryFragmentDirections.actionNavigateToRecoverPassword(
        keystore,
        walletBalance,
        walletAddress, walletName?: walletAddress, isFromOnboarding
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

  fun navigateToSuccess(isFromOnboarding: Boolean) {
    val bottomSheet = RecoveryWalletSuccessBottomSheetFragment.newInstance(isFromOnboarding)
    bottomSheet.show(fragment.parentFragmentManager, "RecoveryWalletSuccess")
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