package com.asfoundation.wallet.restore.intro

import android.net.Uri
import com.asfoundation.wallet.util.RestoreErrorType
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface RestoreWalletView {

  fun restoreFromStringClick(): Observable<String>

  fun restoreFromFileClick(): Observable<Any>

  fun showError(type: RestoreErrorType)

  fun onPermissionsGiven(): PublishSubject<Unit>

  fun onFileChosen(): Observable<Uri>

  fun showWalletRestoreAnimation()

  fun showWalletRestoredAnimation()

  fun showSnackBarError()

  fun hideAnimation()

  fun askForReadPermissions()

  fun hideKeyboard()

  fun setKeystore(keystore: String)
}
