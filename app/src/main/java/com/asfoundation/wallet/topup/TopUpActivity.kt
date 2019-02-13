package com.asfoundation.wallet.topup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.ui.BaseActivity

class TopUpActivity : BaseActivity(), TopUpActivityView, ToolbarManager {
  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, TopUpActivity::class.java)
    }
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.top_up_activity_layout)
    TopUpActivityPresenter(this).present(savedInstanceState == null)
  }

  override fun showTopUpScreen() {
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, TopUpFragment.newInstance()).commit()
  }

  override fun setupToolbar() {
    setTitle(R.string.topup_title)
    toolbar()
  }
}
