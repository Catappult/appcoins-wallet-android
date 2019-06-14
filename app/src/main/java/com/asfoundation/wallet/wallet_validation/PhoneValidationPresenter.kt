package com.asfoundation.wallet.wallet_validation

import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class PhoneValidationPresenter(
    private val view: PhoneValidationView,
    private val activity: WalletValidationActivityView?,
    private val smsValidationInteract: SmsValidationInteract,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) {

  private val disposables: CompositeDisposable = CompositeDisposable()

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
              activity?.finish()
            }.subscribe())
  }

  private fun handleSubmit() {
    disposables.add(
        view.getSubmitClicks()
            .subscribeOn(viewScheduler)
            .observeOn(networkScheduler)
            .flatMapSingle {
              smsValidationInteract.requestValidationCode("+${it.first}${it.second}")
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
      WalletValidationStatus.INVALID_PHONE -> handleError(
          R.string.verification_insert_phone_field_number_error)
      WalletValidationStatus.DOUBLE_SPENT -> handleError(
          R.string.verification_insert_phone_field_phone_used_already_error)
      WalletValidationStatus.GENERIC_ERROR -> handleError(R.string.unknown_error)
    }
  }

  private fun handleError(errorMessage: Int) {
    view.setButtonState(false)
    view.setError(errorMessage)
  }

  private fun handleValuesChange() {
    disposables.add(
        Observable.combineLatest(
            view.getCountryCode().subscribeOn(viewScheduler).observeOn(viewScheduler),
            view.getPhoneNumber().subscribeOn(viewScheduler).observeOn(viewScheduler),
            BiFunction { countryCode: String, phoneNumber: String ->
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