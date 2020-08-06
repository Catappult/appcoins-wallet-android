package com.asfoundation.wallet.wallet_validation.generic

import androidx.annotation.StringRes
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import com.asfoundation.wallet.wallet_validation.generic.PhoneValidationFragment.Companion.PhoneValidationClickData
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class PhoneValidationPresenter(
    private val view: PhoneValidationView,
    private val activity: WalletValidationView?,
    private val smsValidationInteract: SmsValidationInteract,
    private val logger: Logger,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val disposables: CompositeDisposable,
    private val analytics: WalletValidationAnalytics
) {
  companion object {
    private val TAG = PhoneValidationPresenter::class.java.simpleName
  }

  private var cachedValidationStatus: Pair<WalletValidationStatus, PhoneValidationClickData>? =
      null

  fun onResume(errorMessage: Int?) {
    resumePreviousState(errorMessage)
  }

  fun present() {
    view.setupUI()
    handleValuesChange()
    handleNextAndRetryClicks()
    handleCancelAndLaterClicks()
  }

  private fun resumePreviousState(errorMessage: Int?) {
    cachedValidationStatus?.let { onSuccess(it.first, it.second); cachedValidationStatus = null }
    errorMessage?.let {
      view.setError(it)
      view.setButtonState(false)
    }
  }

  private fun handleCancelAndLaterClicks() {
    disposables.add(
        Observable.merge(view.getCancelClicks(), view.getLaterButtonClicks())
            .doOnNext {
              handlePhoneValidationAnalytics("close", WalletValidationStatus.SUCCESS,
                  it.previousContext)
              view.hideKeyboard()
              activity?.finishCancelActivity()
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleNextAndRetryClicks() {
    disposables.add(
        Observable.merge(view.getNextClicks(), view.getRetryButtonClicks())
            .observeOn(viewScheduler)
            .doOnNext { view.setButtonState(false) }
            .observeOn(networkScheduler)
            .flatMapSingle {
              smsValidationInteract.requestValidationCode("${it.countryCode}${it.number}")
                  .observeOn(viewScheduler)
                  .doOnSuccess { status ->
                    cachedValidationStatus = Pair(status, it)
                    view.setButtonState(true)
                    onSuccess(status, it)
                    cachedValidationStatus = null
                  }
                  .doOnError { throwable ->
                    analytics.sendPhoneVerificationEvent("submit", it.previousContext, "error",
                        "generic_error")
                    view.setButtonState(false)
                    showErrorMessage(R.string.unknown_error)
                    logger.log(TAG, throwable.message, throwable)
                  }
            }
            .retry()
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun onSuccess(status: WalletValidationStatus, submitInfo: PhoneValidationClickData) {
    handlePhoneValidationAnalytics("submit", status, submitInfo.previousContext)
    when (status) {
      WalletValidationStatus.SUCCESS -> activity?.showCodeValidationView(submitInfo.countryCode,
          submitInfo.number) ?: run {
        showErrorMessage(R.string.unknown_error)
        logError()
      }
      WalletValidationStatus.TOO_MANY_ATTEMPTS -> showErrorMessage(
          R.string.verification_error_attempts_reached)
      WalletValidationStatus.INVALID_INPUT,
      WalletValidationStatus.INVALID_PHONE -> {
        showErrorMessage(R.string.verification_insert_phone_field_number_error)
        view.setButtonState(false)
      }
      WalletValidationStatus.DOUBLE_SPENT -> {
        showErrorMessage(R.string.verification_insert_phone_field_phone_used_already_error)
        view.setButtonState(false)
      }
      WalletValidationStatus.GENERIC_ERROR -> showErrorMessage(R.string.unknown_error)
      WalletValidationStatus.NO_NETWORK -> {
        view.hideKeyboard()
        view.showNoInternetView()
      }
      WalletValidationStatus.LANDLINE_NOT_SUPPORTED -> {
        showErrorMessage(R.string.verification_insert_phone_field_landline_error)
        view.setButtonState(false)
      }
      WalletValidationStatus.REGION_NOT_SUPPORTED -> {
        showErrorMessage(R.string.verification_insert_phone_field_region_error)
        view.setButtonState(false)
      }
    }
  }

  private fun handlePhoneValidationAnalytics(action: String, status: WalletValidationStatus,
                                             previousContext: String) {
    if (status == WalletValidationStatus.SUCCESS) {
      analytics.sendPhoneVerificationEvent(action, previousContext, "success", "")
    } else {
      analytics.sendPhoneVerificationEvent(action, previousContext, "error", status.name)
    }
  }

  private fun logError() = logger.log(TAG, "Validation Error: Activity null")

  private fun showErrorMessage(@StringRes errorMessage: Int) = view.setError(errorMessage)

  private fun handleValuesChange() {
    disposables.add(
        Observable.combineLatest(
            view.getCountryCode(),
            view.getPhoneNumber(),
            BiFunction { countryCode: String, phoneNumber: String ->
              view.clearError()
              if (hasValidData(countryCode, phoneNumber)) {
                view.setButtonState(true)
              } else {
                view.setButtonState(false)
              }
            })
            .subscribe({ }, { throwable -> throwable.printStackTrace() }))
  }

  private fun hasValidData(countryCode: String, phoneNumber: String): Boolean {
    return phoneNumber.isNotBlank() && countryCode.isNotBlank()
  }

  fun stop() = disposables.dispose()

}