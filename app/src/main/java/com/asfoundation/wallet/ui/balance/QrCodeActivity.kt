package com.asfoundation.wallet.ui.balance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.Window
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.ActivityNavigator
import com.asf.wallet.R
import com.asf.wallet.databinding.QrCodeLayoutBinding
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.util.generateQrCode
import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class QrCodeActivity : BaseActivity(), QrCodeView {

  @Inject
  lateinit var findDefaultWalletInteract: FindDefaultWalletInteract
  private lateinit var presenter: QrCodePresenter

  private var _binding: QrCodeLayoutBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  private val main_layout get() = binding.mainLayout
  private val qr_image get() = binding.qrImage

  override fun onCreate(savedInstanceState: Bundle?) {
    setAnimationOptions()

    super.onCreate(savedInstanceState)

    _binding = QrCodeLayoutBinding.inflate(layoutInflater)
    setContentView(binding.root)
    main_layout.setOnClickListener { onBackPressed() }
    presenter =
        QrCodePresenter(this, findDefaultWalletInteract, CompositeDisposable(),
            AndroidSchedulers.mainThread())
    presenter.present()
  }

  private fun setAnimationOptions() {
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    val fade = Fade()
    fade.excludeTarget(android.R.id.statusBarBackground, true)
    fade.excludeTarget(android.R.id.navigationBarBackground, true)
    window.enterTransition = fade
    window.exitTransition = fade
  }

  override fun onResume() {
    super.onResume()
    sendPageViewEvent()
  }

  override fun createQrCode(walletAddress: String) {
    try {
      val logo = ResourcesCompat.getDrawable(resources, R.drawable.ic_appc_token, null)
      val mergedQrCode = walletAddress.generateQrCode(windowManager, logo!!)
      qr_image.setImageBitmap(mergedQrCode)
    } catch (e: Exception) {
      Snackbar.make(main_layout, getString(R.string.error_fail_generate_qr), Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  override fun onDestroy() {
    presenter.stop()
    super.onDestroy()
    _binding = null
  }

  override fun finish() {
    super.finish()
    ActivityNavigator.applyPopAnimationsToPendingTransition(this)
  }

  companion object {
    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, QrCodeActivity::class.java)
    }
  }
}