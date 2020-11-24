package com.asfoundation.wallet.ui.backup

import io.reactivex.Observable

interface BackupActivityView {

  fun closeScreen()

  fun onSystemFileIntentResult(): Observable<SystemFileIntentResult>

  fun setupToolbar()

  fun onDocumentFile(systemFileIntentResult: SystemFileIntentResult)
}
