package com.asfoundation.wallet.ui.balance

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ShareCompat
import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.MyAddressActivity
import com.asfoundation.wallet.util.generateQrCode
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.copy_share_buttons_layout.*
import kotlinx.android.synthetic.main.qr_code_layout.*
import javax.inject.Inject

class QrCodeActivity : BaseActivity(), QrCodeView {

  @Inject
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
  private lateinit var presenter: QrCodePresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.qr_code_layout)
    presenter =
        QrCodePresenter(this, findDefaultWalletInteract, CompositeDisposable(),
            AndroidSchedulers.mainThread())
    presenter.present()
  }

  override fun copyClick(): Observable<Any> {
    return RxView.clicks(copy_button)
  }

  override fun shareClick(): Observable<Any> {
    return RxView.clicks(share_button)
  }

  override fun closeClick(): Observable<Any> {
    return RxView.clicks(close_btn)
  }

  override fun setAddressToClipBoard(walletAddress: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    val clip = ClipData.newPlainText(
        MyAddressActivity.KEY_ADDRESS, walletAddress)
    if (clipboard != null) {
      clipboard.primaryClip = clip
    }
    Snackbar.make(main_layout, R.string.wallets_address_copied_body, Snackbar.LENGTH_SHORT)
        .show()
  }

  override fun setWalletAddress(walletAddress: String) {
    active_wallet_address.text = walletAddress
  }

  override fun createQrCode(walletAddress: String) {
    try {
      val mergedQrCode = walletAddress.generateQrCode(resources, windowManager)
      qr_image.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(main_layout, getString(R.string.error_fail_generate_qr), Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun showShare(walletAddress: String) {
    ShareCompat.IntentBuilder.from(this)
        .setText(walletAddress)
        .setType("text/plain")
        .setChooserTitle(resources.getString(R.string.referral_share_sheet_title))
        .startChooser()
  }

  override fun onBackPressed() {
    closeSuccess()
    super.onBackPressed()
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
  }

  override fun closeSuccess() {
    setResult(Activity.RESULT_OK, Intent())
    finish()
  }

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, QrCodeActivity::class.java)
    }
  }
}