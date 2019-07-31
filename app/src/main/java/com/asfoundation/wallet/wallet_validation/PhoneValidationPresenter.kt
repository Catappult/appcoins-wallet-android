package com.asfoundation.wallet.wallet_validation

import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class PhoneValidationPresenter(
    private val view: PhoneValidationView,
    private val activity: WalletValidationView?,
    private val smsValidationInteract: SmsValidationInteract,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val disposables: CompositeDisposable
) {

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
            .subscribeOn(viewScheduler)
            .flatMapSingle {
              smsValidationInteract.requestValidationCode("${it.first}${it.second}")
                  .subscribeOn(networkScheduler)
                  .observeOn(viewScheduler)
                  .doOnSuccess { status ->
                    onSuccess(status, it)
                  }
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
      WalletValidationStatus.GENERIC_ERROR -> showErrorMessage(R.string.unknown_error)
    }
  }

  private fun showErrorMessage(errorMessage: Int) {
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
    return phoneNumber.isNotBlank() &&
        countryCode.isNotBlank()
  }

  fun stop() {
    disposables.dispose()
  }

}