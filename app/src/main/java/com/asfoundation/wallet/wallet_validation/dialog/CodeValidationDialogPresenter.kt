package com.asfoundation.wallet.wallet_validation.dialog

import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function6

class CodeValidationDialogPresenter(
    private val dialogView: CodeValidationDialogView,
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
    dialogView.setupUI()
    handleBack()
    handleCode()
    handleResendCode()
    handleValuesChange()
    handleSubmit()
  }

  private fun handleResendCode() {
    disposables.add(
        dialogView.getResentCodeClicks()
            .doOnNext {
              dialogView.clearUI()
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
        dialogView.getSubmitClicks()
            .doOnNext {
              analytics.sendCodeVerificationEvent("submit")
              activity?.showLoading(it)
            }
            .subscribe()
    )
  }

  private fun handleBack() {
    disposables.add(
        dialogView.getBackClicks()
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
            dialogView.getFirstChar(),
            dialogView.getSecondChar(),
            dialogView.getThirdChar(),
            dialogView.getFourthChar(),
            dialogView.getFifthChar(),
            dialogView.getSixthChar(),
            Function6 { first: String, second: String, third: String, fourth: String, fifth: String, sixth: String ->
              if (isValidInput(first, second, third, fourth, fifth, sixth)) {
                dialogView.setButtonState(true)
              } else {
                dialogView.setButtonState(false)
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
    disposables.add(dialogView.getFirstChar()
        .filter { it.isNotBlank() }
        .doOnNext { dialogView.moveToNextView(1) }
        .subscribe()
    )

    disposables.add(dialogView.getSecondChar()
        .filter { it.isNotBlank() }
        .doOnNext { dialogView.moveToNextView(2) }
        .subscribe()
    )

    disposables.add(dialogView.getThirdChar()
        .filter { it.isNotBlank() }
        .doOnNext { dialogView.moveToNextView(3) }
        .subscribe()
    )

    disposables.add(dialogView.getFourthChar()
        .filter { it.isNotBlank() }
        .doOnNext { dialogView.moveToNextView(4) }
        .subscribe()
    )

    disposables.add(dialogView.getFifthChar()
        .filter { it.isNotBlank() }
        .doOnNext { dialogView.moveToNextView(5) }
        .subscribe()
    )

    disposables.add(dialogView.getSixthChar()
        .filter { it.isNotBlank() }
        .doOnNext { dialogView.hideKeyboard() }
        .subscribe()
    )
  }

  fun stop() {
    disposables.dispose()
  }
}