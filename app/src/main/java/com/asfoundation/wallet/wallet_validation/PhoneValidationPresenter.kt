package com.asfoundation.wallet.wallet_validation

import com.asf.wallet.R
import com.asfoundation.wallet.interact.SmsValidationInteract
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import retrofit2.HttpException

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
                  .doOnSuccess { _ -> activity?.showCodeValidationView(it.first, it.second) }
            }
            .doOnError { handleError(it) }
            .retry()
            .subscribe { }
    )
  }

  private fun handleError(throwable: Throwable?) {
    view.setButtonState(false)

    var errorMessage: Int = R.string.unknown_error

    if (throwable is HttpException) {
      errorMessage = when (throwable.code()) {
        400 -> R.string.wallet_validation_phone_number_invalid
        429 -> R.string.wallet_validation_many_requests
        else -> R.string.unknown_error
      }
    }
    view.setError(errorMessage)
  }

  private fun handleValuesChange() {
    disposables.add(
        Observable.combineLatest(
            view.getCountryCode().subscribeOn(viewScheduler).observeOn(viewScheduler),
            view.getPhoneNumber().subscribeOn(viewScheduler).observeOn(viewScheduler),
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