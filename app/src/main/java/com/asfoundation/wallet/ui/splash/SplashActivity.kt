package com.asfoundation.wallet.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class SplashActivity : BaseActivity(), SplashView {

  @Inject
  lateinit var presenter: SplashPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    presenter.present(savedInstanceState)
  }

  public override fun onActivityResult(requestCode: Int, resultCode: Int,
                                       data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    presenter.onActivityResult(requestCode, resultCode, data)
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  public override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstance(outState)
  }

  companion object {
    fun newIntent(context: Context?): Intent {
      return Intent(context, SplashActivity::class.java)
    }
  }
}