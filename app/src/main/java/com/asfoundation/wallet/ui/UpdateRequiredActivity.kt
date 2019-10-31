package com.asfoundation.wallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.navigator.UpdateNavigator
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.update_required_main_layout.*
import javax.inject.Inject

class UpdateRequiredActivity : BaseActivity(), UpdateRequiredView {

  private lateinit var presenter: UpdateRequiredPresenter
  @Inject
  lateinit var updateNavigator: UpdateNavigator
  @Inject
  lateinit var autoUpdateInteract: AutoUpdateInteract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    setContentView(R.layout.update_required_main_layout)
    updateNavigator = UpdateNavigator()
    presenter = UpdateRequiredPresenter(this, CompositeDisposable(), autoUpdateInteract)
    presenter.present()
  }

  override fun navigateToStoreAppView(deepLink: String) {
    updateNavigator.navigateToStoreAppView(this, deepLink)
  }

  override fun updateClick(): Observable<Any> {
    return RxView.clicks(update_button)
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.stop()
  }

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, UpdateRequiredActivity::class.java)
    }
  }
}