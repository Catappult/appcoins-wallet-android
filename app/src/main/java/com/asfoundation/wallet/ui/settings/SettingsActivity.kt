package com.asfoundation.wallet.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity(), SettingsActivityView {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  private var authenticationResultSubject: PublishSubject<Boolean>? = null

  companion object {
    private const val AUTHENTICATION_REQUEST_CODE = 33
    private const val TURN_ON_FINGERPRINT = "turn_on_fingerprint"

    @JvmStatic
    fun newIntent(context: Context, turnOnFingerprint: Boolean = false): Intent {
      val intent = Intent(context, SettingsActivity::class.java)
      return intent.apply {
        putExtra(TURN_ON_FINGERPRINT, turnOnFingerprint)
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    toolbar()
    authenticationResultSubject = PublishSubject.create()
    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction()
        .replace(
          R.id.fragment_container,
          SettingsFragment.newInstance(intent.getBooleanExtra(TURN_ON_FINGERPRINT, false))
        )
        .commit()
    }
    setToolbarListener()
  }

  private fun setToolbarListener() {
    findViewById<FrameLayout>(R.id.action_button_support).setOnClickListener {
      displayChat()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      onBackPressed()
      finish()
      return true
    }
    return super.onOptionsItemSelected(item)
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