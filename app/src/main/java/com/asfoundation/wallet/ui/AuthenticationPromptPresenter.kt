package com.asfoundation.wallet.ui

import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class AuthenticationPromptPresenter(
    private val view: AuthenticationPromptView,
    private val viewScheduler: Scheduler,
    private val ioScheduler: Scheduler,
    private val disposables: CompositeDisposable,
    private val fingerprintInteract: FingerPrintInteract,
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  private var hasBottomsheetOn = false

  companion object {
    private const val BOTTOMSHEET_KEY = "bottomsheet_key"

  }

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      hasBottomsheetOn = it.getBoolean(BOTTOMSHEET_KEY)
    }

    if (!hasBottomsheetOn) showEverything()
    handleAuthenticationResult()
    handleRetryAuthentication()
  }


  private fun showEverything() {

    when (fingerprintInteract.compatibleDevice()) {
      BiometricManager.BIOMETRIC_SUCCESS -> {
        view.showPrompt(view.createBiometricPrompt(),
            fingerprintInteract.definePromptInformation())
      }
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> view.showBottomSheetDialogFragment(
          "Enable Fingerprint Authentication in your phone.")
      //view.firstScreenNavigation()

      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> view.showBottomSheetDialogFragment(
          "Enable Fingerprint Authentication in your phone.")

      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
        if (view.checkBiometricSupport()) {
          view.showPrompt(view.createBiometricPrompt(),
              fingerprintInteract.definePromptInformation())
        } else {
          preferencesRepositoryType.setAuthenticationPermission(false)
          view.closeSuccess()
        }
      }
    }
  }

  private fun setBottomsheetOn(message: String) {
    hasBottomsheetOn = true
    view.showBottomSheetDialogFragment(message)
  }


  private fun handleAuthenticationResult() {
    disposables.add(view.getAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          when (it.type) {
            FingerprintResult.SUCCESS -> view.closeSuccess()
            FingerprintResult.ERROR -> {
              if (it.errorCode == BiometricPrompt.ERROR_USER_CANCELED || it.errorCode == BiometricPrompt.ERROR_CANCELED) {
                view.closeCancel()
              } else {
                setBottomsheetOn(it.errorString.toString())
              }
            }
            FingerprintResult.FAIL -> {
              view.showFail()
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleRetryAuthentication() {
    disposables.add(view.getRetryButtonClick()
        .observeOn(viewScheduler)
        .doOnNext {
          hasBottomsheetOn = false
          showEverything()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(BOTTOMSHEET_KEY, hasBottomsheetOn)
  }


  fun stop() {
    disposables.clear()
  }

}
