package com.asfoundation.wallet.ui

import android.hardware.biometrics.BiometricManager
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class SplashPresenter(
    private val view: SplashView,
    private val viewScheduler: Scheduler,
    private val ioScheduler: Scheduler,
    private val disposables: CompositeDisposable,
    private val autoUpdateInteract: AutoUpdateInteract,
    private val fingerprintInteract: FingerPrintInteract,
    private val preferencesRepositoryType: PreferencesRepositoryType) {

  fun present() {
    handleAutoUpdate()
    handleAuthenticationResult()
    handleRetryAuthentication()
  }


  private fun handleAutoUpdate() {
    disposables.add(autoUpdateInteract.getAutoUpdateModel(true)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { (updateVersionCode, updateMinSdk, blackList) ->
          if (autoUpdateInteract.isHardUpdateRequired(blackList,
                  updateVersionCode, updateMinSdk)) {
            view.navigateToAutoUpdate()
          } else {
            if (preferencesRepositoryType.hasAuthenticationPermission()) {

              when (fingerprintInteract.compatibleDevice()) {

                BiometricManager.BIOMETRIC_SUCCESS -> {
                  view.showPrompt(view.createBiometricPrompt(),
                      fingerprintInteract.definePromptInformation())
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> view.firstScreenNavigation()

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> view.showBottomSheetDialogFragment(
                    "Enable Fingerprint Authentication in your phone.")

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                  if (view.checkBiometricSupport()) {
                    view.showBottomSheetDialogFragment(
                        "No fingerprints associated yet! Try again with pin.")
                  } else {
                    //Se nenhuma fingerprint nem pin estiverem definidos, então mudar o state do switch para false e continuar
                    preferencesRepositoryType.setAuthenticationPermission(false)
                    view.firstScreenNavigation()
                  }

                }

              }

            } else {
              view.firstScreenNavigation()
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.getAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          when (it.type) {
            FingerprintResult.SUCCESS -> view.firstScreenNavigation()
            FingerprintResult.ERROR -> view.showBottomSheetDialogFragment(it.errorString)
            FingerprintResult.FAIL -> view.showFail()
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

  fun stop() {
    disposables.clear()
  }


}