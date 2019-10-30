package com.asfoundation.wallet.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.asf.wallet.R
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.update_required_main_layout.*
import javax.inject.Inject

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

  override fun navigateToStoreAppView(deepLink: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val packageManager = packageManager
    val appsList =
        packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    appsList?.let {
      for (info in it) {
        if (info.activityInfo.packageName == "cm.aptoide.pt") {
          intent.setPackage(info.activityInfo.packageName)
          break
        }
        if (info.activityInfo.packageName == "com.android.vending")
          intent.setPackage(info.activityInfo.packageName)
      }
    }
    this.startActivity(intent)
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