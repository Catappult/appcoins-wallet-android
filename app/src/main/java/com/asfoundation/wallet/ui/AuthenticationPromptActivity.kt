package com.asfoundation.wallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticationPromptActivity : BaseActivity(), AuthenticationPromptView {

  @Inject
  lateinit var fingerprintInteractor: FingerprintInteractor

  private lateinit var presenter: AuthenticationPromptPresenter

  private var fingerprintResultSubject: PublishSubject<FingerprintAuthResult>? = null

  private var retryClickSubject: PublishSubject<Any>? = null

  companion object {
    const val RESULT_OK = 0
    const val RESULT_CANCELED = 1

    @JvmStatic
    fun newIntent(context: Context): Intent {
      return Intent(context, AuthenticationPromptActivity::class.java)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.authentication_prompt_activity)
    retryClickSubject = PublishSubject.create<Any>()
    fingerprintResultSubject = PublishSubject.create<FingerprintAuthResult>()
    presenter = AuthenticationPromptPresenter(this, AndroidSchedulers.mainThread(),
        CompositeDisposable(), fingerprintInteractor)
    presenter.present(savedInstanceState)
  }

  override fun createBiometricPrompt(): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(this)
    return BiometricPrompt(this, executor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            fingerprintResultSubject?.onNext(
                FingerprintAuthResult(errorCode, errString.toString(), null,
                    FingerprintResult.ERROR))
          }

          override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            fingerprintResultSubject?.onNext(
                FingerprintAuthResult(null, null, result, FingerprintResult.SUCCESS))
          }

          override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            fingerprintResultSubject?.onNext(
                FingerprintAuthResult(null, null, null, FingerprintResult.FAIL))
          }
        })
  }



  override fun onResume() {
    super.onResume()
    presenter.onResume()
    sendPageViewEvent()
  }

  override fun getAuthenticationResult(): Observable<FingerprintAuthResult> {
    return fingerprintResultSubject!!
  }

  override fun getRetryButtonClick(): Observable<Any> {
    return retryClickSubject!!
  }

  override fun onRetryButtonClick() {
    retryClickSubject?.onNext("")
  }

  override fun showAuthenticationBottomSheet(timer: Long) {
    supportFragmentManager.beginTransaction()
        .setCustomAnimations(R.anim.fade_in_animation, R.anim.fragment_slide_down,
            R.anim.fade_in_animation, R.anim.fragment_slide_down)
        .replace(R.id.bottom_sheet_error_fragment_container,
            AuthenticationErrorFragment.newInstance(timer))
        .commit()
  }

  override fun showPrompt(biometricPrompt: BiometricPrompt, deviceCredentialsAllowed: Boolean) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(R.string.fingerprint_authentication_required_title))
        .setSubtitle(getString(R.string.fingerprint_authentication_required_body))
        .setDeviceCredentialAllowed(deviceCredentialsAllowed)
        .build()
    biometricPrompt.authenticate(promptInfo)
  }

  override fun closeSuccess() {
    val intent = Intent()
    setResult(RESULT_OK, intent)
    finishAndRemoveTask()
  }

  override fun closeCancel() {
    val intent = Intent()
    setResult(RESULT_CANCELED, intent)
    finishAndRemoveTask()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSaveInstanceState(outState)
  }

  override fun onBackPressed() = closeCancel()

  override fun onDestroy() {
    fingerprintResultSubject = null
    retryClickSubject = null
    presenter.stop()
    super.onDestroy()
  }
}