package com.asfoundation.wallet.verification

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import com.asf.wallet.R
import com.asfoundation.wallet.restore.intro.RestoreWalletFragment
import com.asfoundation.wallet.ui.BaseActivity
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class VerificationActivity : BaseActivity(), VerificationActivityView {

  companion object {
    @JvmStatic
    fun newIntent(context: Context) = Intent(context, VerificationActivity::class.java)
  }

  @Inject
  lateinit var presenter: VerificationActivityPresenter

  private val toolbarBackPressSubject = PublishSubject.create<String>()

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_verification)
    toolbar()
    presenter.present(savedInstanceState)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      toolbarBackPressSubject.onNext(
          supportFragmentManager.findFragmentById(R.id.fragment_container)?.javaClass?.name ?: "")
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun getCurrentFragment(): String {
    val fragments = supportFragmentManager.fragments
    return if (fragments.isNotEmpty()) fragments[0]::class.java.simpleName
    else RestoreWalletFragment::class.java.simpleName
  }

  override fun cancel() = finish()

  override fun complete() = finish()

  override fun lockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
  }

  override fun unlockRotation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  override fun getToolbarBackPressEvents(): Observable<String> {
    return toolbarBackPressSubject
  }
}
