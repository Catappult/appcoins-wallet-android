package com.asfoundation.wallet.fingerprint

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity

class ManageFingerprintActivity : BaseActivity() {
  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, ManageFingerprintActivity::class.java)
    }
  }

  private fun setupToolbar() {
    toolbar()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_fingerprint_layout)
    setupToolbar()
    if (savedInstanceState == null) {
      showFingerprintFragment()
    }
  }

  private fun showFingerprintFragment() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, FingerprintFragment.newInstance())
        .commit()
  }
}
