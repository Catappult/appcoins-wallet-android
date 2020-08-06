package com.asfoundation.wallet.ui

import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import android.util.Log
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


  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) showEverything()
    //handleAuthenticationResult()
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
          view.showBottomSheetDialogFragment(
              "No fingerprints associated yet! Try again with pin.")
        } else {
          preferencesRepositoryType.setAuthenticationPermission(false)
          view.closeSuccess()
        }
      }
    }
  }


  private fun handleAuthenticationResult() {
    disposables.add(view.getAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          when (it.type) {
            FingerprintResult.SUCCESS -> view.closeSuccess()
            FingerprintResult.ERROR -> {
              if (it.errorCode == BiometricPrompt.ERROR_USER_CANCELED || it.errorCode == BiometricPrompt.ERROR_CANCELED) {
                Log.d("TAG123", "PROMPT ERROR " + it.errorCode.toString())
                view.closeCancel()
              } else {
                Log.d("TAG123", "PROMPT ERROR " + it.errorCode.toString())
                view.showBottomSheetDialogFragment(it.errorString.toString())
              }
            }
            FingerprintResult.FAIL -> {
              Log.d("TAG123", "CHEGOU AO FAIL?")
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
          view.showPrompt(view.createBiometricPrompt(),
              fingerprintInteract.definePromptInformation())
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun handleAuthenticationResult(result : FingerprintAuthResult){
    when (result.type) {
      FingerprintResult.SUCCESS -> view.closeSuccess()
      FingerprintResult.ERROR -> {
        if (result.errorCode == BiometricPrompt.ERROR_USER_CANCELED || result.errorCode == BiometricPrompt.ERROR_CANCELED) {
          Log.d("TAG123", "PROMPT ERROR " + result.errorCode.toString())
          view.closeCancel()
        } else {
          Log.d("TAG123", "PROMPT ERROR " + result.errorCode.toString())
          view.showBottomSheetDialogFragment(result.errorString.toString())
        }
      }
      FingerprintResult.FAIL -> {
        Log.d("TAG123", "CHEGOU AO FAIL?")
        view.showFail()
      }
    }

  }

  fun stop() {
    disposables.clear()
  }

}
