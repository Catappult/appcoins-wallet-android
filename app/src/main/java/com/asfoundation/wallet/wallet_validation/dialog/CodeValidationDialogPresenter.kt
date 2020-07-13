package com.asfoundation.wallet.wallet_validation.dialog

import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function6

class CodeValidationDialogPresenter(
    private val view: CodeValidationDialogView,
    private val activity: WalletValidationDialogView?,
    private val smsValidationInteract: SmsValidationInteract,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val countryCode: String,
    private val phoneNumber: String,
    private val disposables: CompositeDisposable,
    private val analytics: WalletValidationAnalytics
) {

  fun present() {
    view.setupUI()
    handleBack()
    handleCode()
    handleResendCode()
    handleValuesChange()
    handleSubmit()
  }

  private fun handleResendCode() {
    disposables.add(
        view.getResentCodeClicks()
            .doOnNext {
              view.clearUI()
            }
            .subscribeOn(viewScheduler)
            .flatMapSingle {
              smsValidationInteract.requestValidationCode("+$countryCode$phoneNumber")
                  .subscribeOn(networkScheduler)
            }
            .retry()
            .subscribe()
    )
  }

  private fun handleSubmit() {
    disposables.add(
        view.getSubmitClicks()
            .doOnNext {
              analytics.sendCodeVerificationEvent("submit")
              activity?.showLoading(it)
            }
            .subscribe()
    )
  }

  private fun handleBack() {
    disposables.add(
        view.getBackClicks()
            .doOnNext {
              analytics.sendCodeVerificationEvent("back")
              activity?.showPhoneValidationView(countryCode, phoneNumber)
            }
            .subscribe()
    )
  }

  private fun handleValuesChange() {
    disposables.add(
        Observable.combineLatest(
            view.getFirstChar(),
            view.getSecondChar(),
            view.getThirdChar(),
            view.getFourthChar(),
            view.getFifthChar(),
            view.getSixthChar(),
            Function6 { first: String, second: String, third: String, fourth: String, fifth: String, sixth: String ->
              if (isValidInput(first, second, third, fourth, fifth, sixth)) {
                view.setButtonState(true)
              } else {
                view.setButtonState(false)
              }
            })
            .subscribe { })
  }

  private fun isValidInput(first: String, second: String, third: String,
                           fourth: String, fifth: String, sixth: String): Boolean {
    return first.isNotBlank() &&
        second.isNotBlank() &&
        third.isNotBlank() &&
        fourth.isNotBlank() &&
        fifth.isNotBlank() &&
        sixth.isNotBlank()
  }

  private fun handleCode() {
    disposables.add(view.getFirstChar()
        .filter { it.isNotBlank() }
        .doOnNext { view.moveToNextView(1) }
        .subscribe()
    )

    disposables.add(view.getSecondChar()
        .filter { it.isNotBlank() }
        .doOnNext { view.moveToNextView(2) }
        .subscribe()
    )

    disposables.add(view.getThirdChar()
        .filter { it.isNotBlank() }
        .doOnNext { view.moveToNextView(3) }
        .subscribe()
    )

    disposables.add(view.getFourthChar()
        .filter { it.isNotBlank() }
        .doOnNext { view.moveToNextView(4) }
        .subscribe()
    )

    disposables.add(view.getFifthChar()
        .filter { it.isNotBlank() }
        .doOnNext { view.moveToNextView(5) }
        .subscribe()
    )

    disposables.add(view.getSixthChar()
        .filter { it.isNotBlank() }
        .doOnNext { view.hideKeyboard() }
        .subscribe()
    )
  }

  fun stop() {
    disposables.dispose()
  }
}