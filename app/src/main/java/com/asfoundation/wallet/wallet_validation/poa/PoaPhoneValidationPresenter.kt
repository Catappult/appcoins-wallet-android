package com.asfoundation.wallet.wallet_validation.poa

import androidx.annotation.StringRes
import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class PoaPhoneValidationPresenter(
    private val view: PoaPhoneValidationView,
    private val activity: PoaWalletValidationView?,
    private val smsValidationInteract: SmsValidationInteract,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val disposables: CompositeDisposable
) {

  private var cachedValidationStatus: Pair<WalletValidationStatus, Pair<String, String>>? = null

  fun present() {
    view.setupUI()
    handleValuesChange()
    handleSubmit()
    handleCancel()
  }

  private fun handleCancel() {
    disposables.add(
        view.getCancelClicks()
            .doOnNext {
              activity?.closeCancel(true)
            }.subscribe())
  }

  private fun handleSubmit() {
    disposables.add(
        view.getSubmitClicks()
            .doOnNext { view.setButtonState(false) }
            .subscribeOn(viewScheduler)
            .flatMapSingle {
              smsValidationInteract.requestValidationCode("${it.first}${it.second}")
                  .subscribeOn(networkScheduler)
                  .observeOn(viewScheduler)
                  .doOnSuccess { status ->
                    cachedValidationStatus = Pair(status, it)
                    view.setButtonState(true)
                    onSuccess(status, it)
                  }
                  .doOnError { view.setButtonState(true) }
                  .doOnSuccess { cachedValidationStatus = null }
            }
            .retry()
            .subscribe { }
    )
  }

  private fun onSuccess(status: WalletValidationStatus, submitInfo: Pair<String, String>) {
    when (status) {
      WalletValidationStatus.SUCCESS -> activity?.showCodeValidationView(submitInfo.first,
          submitInfo.second)
      WalletValidationStatus.INVALID_INPUT,
      WalletValidationStatus.INVALID_PHONE -> {
        showErrorMessage(R.string.verification_insert_phone_field_number_error)
        view.setButtonState(false)
      }
      WalletValidationStatus.DOUBLE_SPENT -> {
        showErrorMessage(R.string.verification_insert_phone_field_phone_used_already_error)
        view.setButtonState(false)
      }
      WalletValidationStatus.NO_NETWORK,
      WalletValidationStatus.GENERIC_ERROR -> showErrorMessage(R.string.unknown_error)
      WalletValidationStatus.LANDLINE_NOT_SUPPORTED ->  {
        showErrorMessage(R.string.verification_insert_phone_field_landline_error)
        view.setButtonState(false)
      }
      WalletValidationStatus.REGION_NOT_SUPPORTED ->  {
        showErrorMessage(R.string.verification_insert_phone_field_region_error)
        view.setButtonState(false)
      }
    }
  }

  private fun showErrorMessage(@StringRes errorMessage: Int) {
    view.setError(errorMessage)
  }

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

  fun stop() {
    disposables.dispose()
  }

  fun onResume() {
    resumePreviousState()
  }

  private fun resumePreviousState() {
    cachedValidationStatus?.let { onSuccess(it.first, it.second); cachedValidationStatus = null }
  }
}