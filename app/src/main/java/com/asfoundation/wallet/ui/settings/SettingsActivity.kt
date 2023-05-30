package com.asfoundation.wallet.ui.settings

import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

@AndroidEntryPoint
class SettingsActivity : BaseActivity(), SettingsActivityView {

  private var authenticationResultSubject: PublishSubject<Boolean>? = null

  companion object {
    private const val AUTHENTICATION_REQUEST_CODE = 33
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    authenticationResultSubject = PublishSubject.create()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == AUTHENTICATION_REQUEST_CODE)
      if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
        authenticationResultSubject?.onNext(true)
      }
  }

  override fun authenticationResult(): Observable<Boolean> = authenticationResultSubject!!

  override fun onDestroy() {
    authenticationResultSubject = null
    super.onDestroy()
  }
}
