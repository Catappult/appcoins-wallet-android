package com.asfoundation.wallet.ui

import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.ceil

class AuthenticationPromptPresenter(private val view: AuthenticationPromptView,
                                    private val viewScheduler: Scheduler,
                                    private val disposables: CompositeDisposable,
                                    private val fingerprintInteractor: FingerprintInteractor) {

  private var hasBottomsheetOn = false

  companion object {
    private const val BOTTOMSHEET_KEY = "bottomsheet_key"
    private const val ERROR_RETRY_TIME_IN_MILLIS = 30000
  }

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      hasBottomsheetOn = it.getBoolean(BOTTOMSHEET_KEY)
    }
    handleAuthenticationResult()
    handleRetryAuthentication()
  }

  private fun showBiometricPrompt() {
    when (fingerprintInteractor.getDeviceCompatibility()) {
      BiometricManager.BIOMETRIC_SUCCESS -> view.showPrompt(view.createBiometricPrompt(), true)
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> view.closeSuccess()
      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> view.closeSuccess()
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
        fingerprintInteractor.setAuthenticationPermission(false)
        view.closeSuccess()
      }
    }
  }

  private fun showBottomSheet(timer: Long) {
    hasBottomsheetOn = true
    view.showAuthenticationBottomSheet(timer)
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.getAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          when (it.type) {
            FingerprintResult.SUCCESS -> view.closeSuccess()
            FingerprintResult.ERROR -> {
              when (it.errorCode) {
                BiometricPrompt.ERROR_LOCKOUT -> showBottomSheet(getAuthenticationTimer())
                //This event needs to be ignored to allow rotation and to allow user to send app to background and then foreground
                BiometricPrompt.ERROR_CANCELED -> Unit
                else -> view.closeCancel()
              }
            }
            /*FingerprintResult.Fail happens when user fails authentication using, for example, a fingerprint that isn't associated yet
            * Also, the Biometric library already shows a fail message withing the prompt.*/
            FingerprintResult.FAIL -> Unit
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleRetryAuthentication() {
    disposables.add(view.getRetryButtonClick()
        .observeOn(viewScheduler)
        .doOnNext {
          hasBottomsheetOn = false
          view.closeCancel()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun getAuthenticationTimer(): Long {
    val lastAuthenticationErrorTime = fingerprintInteractor.getAuthenticationErrorTime()
    val currentTime = System.currentTimeMillis()
    return if (currentTime - lastAuthenticationErrorTime >= ERROR_RETRY_TIME_IN_MILLIS) {
      fingerprintInteractor.setAuthenticationErrorTime(currentTime)
      30
    } else {
      val time =
          (ERROR_RETRY_TIME_IN_MILLIS - (currentTime - lastAuthenticationErrorTime)).toDouble()
      ceil(time / 1000).toLong()
    }
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(BOTTOMSHEET_KEY, hasBottomsheetOn)
  }

  fun stop() = disposables.clear()

  fun onResume() {
    //On resume to allow rotation and to allow the user to send the app to background and then to foregorund and keep the auth dialog
    if (!hasBottomsheetOn) showBiometricPrompt()
  }

}
