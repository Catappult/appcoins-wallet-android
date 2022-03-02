package com.asfoundation.wallet.ui.backup

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.backup.entry.BackupWalletFragment
import javax.inject.Inject

class BackupActivityNavigator @Inject constructor(private val activity: Activity) {

  private val fragmentManager = (activity as AppCompatActivity).supportFragmentManager

  private companion object {
    private const val FILE_NAME_EXTRA_KEY = "file_name"
  }

  fun openSystemFileDirectory(fileName: String) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
      putExtra(FILE_NAME_EXTRA_KEY, fileName)
    }
    activity.startActivityForResult(intent, BackupActivity.ACTION_OPEN_DOCUMENT_TREE_REQUEST_CODE)
  }

  fun showBackupScreen(walletAddress: String) {
    fragmentManager.beginTransaction()
      .replace(R.id.fragment_container, BackupWalletFragment.newInstance(walletAddress))
      .commit()
  }
}
