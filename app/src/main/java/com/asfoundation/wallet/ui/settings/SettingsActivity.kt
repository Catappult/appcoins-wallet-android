package com.asfoundation.wallet.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.R
import com.asf.wallet.databinding.ActivitySettingsBinding
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.AuthenticationPromptActivity
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity(), SettingsActivityView {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  private var authenticationResultSubject: PublishSubject<Boolean>? = null

  private val binding by viewBinding(ActivitySettingsBinding::bind)

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
    authenticationResultSubject = PublishSubject.create()
    if (savedInstanceState == null) {
      supportFragmentManager
        .beginTransaction()
        .replace(
          R.id.fragment_container,
          SettingsFragment.newInstance(intent.getBooleanExtra(TURN_ON_FINGERPRINT, false))
        )
        .commit()
    }
    setToolbar()
  }

  private fun setToolbar() {
    binding.appBar.setContent {
      TopBar(isMainBar = false, onClickSupport = { displayChat() })
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
