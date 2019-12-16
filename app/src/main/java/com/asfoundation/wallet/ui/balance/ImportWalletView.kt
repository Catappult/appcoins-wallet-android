package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.util.ImportErrorType
import com.google.gson.JsonObject
import io.reactivex.Observable

interface ImportWalletView {
  fun importFromStringClick(): Observable<String>
  fun importFromFileClick(): Observable<Any>
  fun launchFileIntent()
  fun fileImported(): Observable<JsonObject>
  fun navigateToPasswordView()

  fun showWalletImportAnimation()
  fun showWalletImportedAnimation()
  fun showError(type: ImportErrorType)
  fun fileChosen()
  fun hideAnimation()
}
