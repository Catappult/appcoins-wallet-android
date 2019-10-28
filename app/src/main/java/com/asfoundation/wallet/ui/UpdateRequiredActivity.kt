package com.asfoundation.wallet.ui

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import com.asf.wallet.R
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.router.ExternalBrowserRouter
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class UpdateRequiredActivity : BaseActivity(), UpdateRequiredView {

  private lateinit var presenter: UpdateRequiredPresenter
  @Inject
  lateinit var autoUpdateInteract: AutoUpdateInteract
  private lateinit var externalBrowserRouter: ExternalBrowserRouter

  override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    super.onCreate(savedInstanceState, persistentState)
    AndroidInjection.inject(this)
    setContentView(R.layout.update_required_main_layout)

    externalBrowserRouter = ExternalBrowserRouter()
    presenter = UpdateRequiredPresenter(this, CompositeDisposable(), autoUpdateInteract)
    presenter.present()
  }

  override fun navigateToStoreAppView(deepLink: String) {
    externalBrowserRouter.open(this, Uri.parse(deepLink))
  }

  override fun showError() {

  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.stop()
  }
}