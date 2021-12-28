package com.asfoundation.wallet.recover

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher

class RecoverWalletNavigator(private val recoverWalletFragment: RecoverWalletFragment) {

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
}