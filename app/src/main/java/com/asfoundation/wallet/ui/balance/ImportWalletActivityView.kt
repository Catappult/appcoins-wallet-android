package com.asfoundation.wallet.ui.balance

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface ImportWalletActivityView {

  fun navigateToPasswordView(keystore: String)

  fun showWalletImportAnimation()

  fun showWalletImportedAnimation()

  fun launchFileIntent(path: Uri?)

  fun hideAnimation()

  fun onFileChosen(): Observable<Uri>
  fun askForReadPermissions()
  fun onPermissionsGiven(): PublishSubject<Unit>
}
