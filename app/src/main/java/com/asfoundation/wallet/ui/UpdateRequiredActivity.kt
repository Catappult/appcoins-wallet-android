package com.asfoundation.wallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.update_required_main_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class UpdateRequiredActivity : BaseActivity(), UpdateRequiredView {

  private lateinit var presenter: UpdateRequiredPresenter

  @Inject
  lateinit var autoUpdateInteract: AutoUpdateInteract

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidInjection.inject(this)
    setContentView(R.layout.update_required_main_layout)
    presenter = UpdateRequiredPresenter(this, CompositeDisposable(), autoUpdateInteract)
    presenter.present()
  }

  override fun onResume() {
    super.onResume()
    sendPageViewEvent()
  }

  override fun navigateToIntent(intent: Intent) = startActivity(intent)

  override fun updateClick() = RxView.clicks(update_button)

  override fun showError() =
      Snackbar.make(main_layout, R.string.unknown_error, Snackbar.LENGTH_SHORT)

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