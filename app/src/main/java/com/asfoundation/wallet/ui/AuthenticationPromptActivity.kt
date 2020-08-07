package com.asfoundation.wallet.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.asf.wallet.R
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AuthenticationPromptActivity : BaseActivity(), AuthenticationPromptView {

  @Inject
  lateinit var preferencesRepositoryType: PreferencesRepositoryType

  @Inject
  lateinit var fingerprintInteract: FingerPrintInteract

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
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.authentication_prompt_activity)
    retryClickSubject = PublishSubject.create<Any>()
    fingerprintResultSubject = PublishSubject.create<FingerprintAuthResult>()

    presenter =
        AuthenticationPromptPresenter(this,
            AndroidSchedulers.mainThread(),
            Schedulers.io(),
            CompositeDisposable(),
            fingerprintInteract,
            preferencesRepositoryType)

    presenter.present(savedInstanceState)


  }


  override fun createBiometricPrompt(): BiometricPrompt {
    val executor =
        ContextCompat.getMainExecutor(this)
    return BiometricPrompt(this, executor,
        object : BiometricPrompt.AuthenticationCallback() {
          override fun onAuthenticationError(errorCode: Int,
                                             errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            fingerprintResultSubject?.onNext(
                FingerprintAuthResult(errorCode, errString, null, FingerprintResult.ERROR))
          }

          override fun onAuthenticationSucceeded(
              result: BiometricPrompt.AuthenticationResult) {
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

  override fun getAuthenticationResult(): Observable<FingerprintAuthResult> {
    return fingerprintResultSubject!!
  }

  override fun getRetryButtonClick(): Observable<Any> {
    return retryClickSubject!!
  }

  override fun onRetryButtonClick() {
    retryClickSubject!!.onNext("")
  }


  override fun showBottomSheetDialogFragment(message: String) {
    val bottomSheetFragment =
        AuthenticationErrorBottomSheetFragment.newInstance(message)
    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
  }

  override fun showPrompt(biometricPrompt: BiometricPrompt,
                          promptInfo: PromptInfo) {
    biometricPrompt.authenticate(promptInfo)
  }

  override fun showFail() {}

  override fun checkBiometricSupport(): Boolean {
    val keyguardManager =
        getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardSecure
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

  override fun onDestroy() {
    fingerprintResultSubject = null
    retryClickSubject = null
    presenter.stop()
    super.onDestroy()
  }


}