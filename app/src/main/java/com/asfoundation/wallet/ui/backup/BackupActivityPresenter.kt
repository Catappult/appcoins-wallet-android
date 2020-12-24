package com.asfoundation.wallet.ui.backup

import android.content.Context
import android.content.Intent
import androidx.documentfile.provider.DocumentFile
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.backup.creation.BackupCreationFragment

class BackupActivityPresenter(private val view: BackupActivityView,
                              private val data: BackupActivityData,
                              private val navigator: BackupActivityNavigator,
                              private val eventSender: WalletsEventSender) {

  fun present(isCreating: Boolean) {
    view.setupToolbar()
    if (isCreating) navigator.showBackupScreen(data.walletAddress)
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, context: Context) {
    if (requestCode == BackupActivity.ACTION_OPEN_DOCUMENT_TREE_REQUEST_CODE) {
      var systemFileIntentResult = SystemFileIntentResult()
      if (resultCode == BaseActivity.RESULT_OK && data != null) {
        data.data?.let {
          val documentFile = DocumentFile.fromTreeUri(context, it)
          systemFileIntentResult = SystemFileIntentResult(documentFile)
        }
      }
      view.onDocumentFile(systemFileIntentResult)
    }
  }

  fun sendWalletSaveFileEvent(currentFragment: String) {
    if (currentFragment == BackupCreationFragment::class.java.simpleName) {
      eventSender.sendWalletSaveFileEvent(WalletsAnalytics.ACTION_BACK,
          WalletsAnalytics.STATUS_FAIL, WalletsAnalytics.REASON_CANCELED)
    }
  }
}
