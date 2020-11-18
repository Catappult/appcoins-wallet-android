package com.asfoundation.wallet.restore

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface RestoreWalletActivityView {

  fun showWalletRestoreAnimation()

  fun showWalletRestoredAnimation()

  fun hideAnimation()

  fun onFileChosen(): Observable<Uri>

  fun askForReadPermissions()

  fun onPermissionsGiven(): PublishSubject<Unit>

  fun getCurrentFragment(): String

  fun endActivity()
}
