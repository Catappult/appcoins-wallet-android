package com.asfoundation.wallet.restore.intro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.navigator.ActivityNavigatorContract
import com.asfoundation.wallet.restore.RestoreWalletActivity
import com.asfoundation.wallet.restore.password.RestoreWalletPasswordFragment
import io.reactivex.Completable
import javax.inject.Inject

class RestoreWalletNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                             private val activityNavigator: ActivityNavigatorContract) {

  fun launchFileIntent(path: Uri?): Completable {
    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
      type = "*/*"
      path?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) putExtra(
            DocumentsContract.EXTRA_INITIAL_URI, it)
      }
    }
    return try {
      activityNavigator.startActivityForResult(intent, R.string.import_wallet_title,
          RestoreWalletActivity.FILE_INTENT_CODE)
      Completable.complete()
    } catch (e: ActivityNotFoundException) {
      Completable.error(e)
    }
  }

  fun navigateToPasswordView(keystore: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, RestoreWalletPasswordFragment.newInstance(keystore))
        .addToBackStack(RestoreWalletPasswordFragment::class.java.simpleName)
        .commit()
  }
}
