package com.asfoundation.wallet.wallet_validation.generic

import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.util.scaleToString
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function6
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class CodeValidationPresenter(
    private val view: CodeValidationView,
    private val activity: WalletValidationView?,
    private val referralInteractor: ReferralInteractorContract,
    private val smsValidationInteract: SmsValidationInteract,
    private val defaultWalletInteract: FindDefaultWalletInteract,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler,
    private val countryCode: String,
    private val phoneNumber: String,
    private val disposables: CompositeDisposable,
    private val hasBeenInvitedFlow: Boolean,
    private val analytics: WalletValidationAnalytics
) {

  var isLastStep = false

  fun present(lastStep: Boolean) {
    view.setupUI()
    handleBack()
    handleCode()
    handleResendCode()
    handleValuesChange()
    handleSubmitAndRetryClicks()
    handleOkClicks()
    handleLaterClicks()

    isLastStep = lastStep
    if (lastStep) {
      checkReferralAvailability()
    }
  }

  private fun handleLaterClicks() {
    disposables.add(
        view.getLaterButtonClicks()
            .doOnNext {
              analytics.sendCodeVerificationEvent("later")
              activity?.finishCancelActivity()
            }
            .subscribe({}, { it.printStackTrace() }))
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
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleSubmitAndRetryClicks() {
    disposables.add(
        Observable.merge(view.getSubmitClicks(), view.getRetryButtonClicks())
            .doOnEach { view.hideNoInternetView() }
            .doOnEach { view.showLoading() }
            .doOnEach { analytics.sendCodeVerificationEvent("next") }
            .flatMapSingle { validationInfo ->
              defaultWalletInteract.find()
                  .delay(2, TimeUnit.SECONDS)
                  .flatMap { wallet ->
                    smsValidationInteract.validateCode(
                        "+${validationInfo.countryCode}${validationInfo.phoneNumber}",
                        wallet,
                        "${validationInfo.code1}${validationInfo.code2}${validationInfo.code3}${validationInfo.code4}${validationInfo.code5}${validationInfo.code6}")
                        .observeOn(viewScheduler)
                        .doOnSuccess { handleNext(it, validationInfo) }
                  }
                  .subscribeOn(networkScheduler)
            }
            .retry()
            .observeOn(viewScheduler)
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleBack() {
    disposables.add(
        view.getBackClicks()
            .doOnNext {
              analytics.sendCodeVerificationEvent("back")
              activity?.showPhoneValidationView(countryCode, phoneNumber)
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleOkClicks() {
    disposables.add(
        view.getOkClicks()
            .doOnNext { activity?.finishSuccessActivity() }
            .subscribe()
    )
  }

  private fun checkReferralAvailability() {
    if (!hasBeenInvitedFlow) {
      view.showGenericValidationComplete()
      isLastStep = true
    } else {
      disposables.add(referralInteractor.retrieveReferral()
          .subscribeOn(networkScheduler)
          .observeOn(viewScheduler)
          .doOnSuccess {
            handleReferralStatus(it.invited, it.symbol, it.maxAmount, it.pendingAmount,
                it.minAmount)
            isLastStep = true
          }
          .subscribe()
      )
    }
  }

  private fun handleReferralStatus(eligible: Boolean, currency: String, maxAmount: BigDecimal,
                                   pendingAmount: BigDecimal, minAmount: BigDecimal) {
    if (eligible) {
      view.showReferralEligible(currency, pendingAmount.scaleToString(2),
          minAmount.scaleToString(2))
    } else {
      view.showReferralIneligible(currency, maxAmount.scaleToString(2))
    }
  }

  private fun handleNext(status: WalletValidationStatus,
                         validationInfo: ValidationInfo) {
    handleVerificationAnalytics(status)
    when (status) {
      WalletValidationStatus.SUCCESS -> checkReferralAvailability()
      WalletValidationStatus.INVALID_CODE,
      WalletValidationStatus.INVALID_INPUT -> handleError(
          R.string.verification_insert_code_error, validationInfo)
      WalletValidationStatus.INVALID_PHONE ->
        activity?.showPhoneValidationView(validationInfo.countryCode, validationInfo.phoneNumber,
            R.string.verification_insert_code_error_common)
      WalletValidationStatus.DOUBLE_SPENT -> checkReferralAvailability()
      WalletValidationStatus.TOO_MANY_ATTEMPTS -> handleError(
          R.string.verification_error_attempts_reached, validationInfo)
      WalletValidationStatus.EXPIRED_CODE -> handleError(R.string.verification_error_time_expired,
          validationInfo)
      WalletValidationStatus.GENERIC_ERROR -> handleError(R.string.unknown_error, validationInfo)
      WalletValidationStatus.NO_NETWORK -> {
        view.hideKeyboard()
        view.showNoInternetView()
      }
    }
  }

  private fun handleVerificationAnalytics(status: WalletValidationStatus) {
    if (status == WalletValidationStatus.SUCCESS) {
      analytics.sendConfirmationEvent("success", "")
    } else {
      analytics.sendConfirmationEvent("error", status.name)
    }
  }

  private fun handleError(errorMessage: Int, validationInfo: ValidationInfo) {
    activity?.showCodeValidationView(validationInfo, errorMessage)
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
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun isValidInput(first: String, second: String, third: String, fourth: String,
                           fifth: String, sixth: String): Boolean {
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
        .doOnNext {
          view.moveToNextView(1)
        }
        .subscribe({}, { it.printStackTrace() }))

    disposables.add(view.getSecondChar()
        .filter { it.isNotBlank() }
        .doOnNext {
          view.moveToNextView(2)
        }
        .subscribe({}, { it.printStackTrace() }))

    disposables.add(view.getThirdChar()
        .filter { it.isNotBlank() }
        .doOnNext {
          view.moveToNextView(3)
        }
        .subscribe({}, { it.printStackTrace() }))

    disposables.add(view.getFourthChar()
        .filter { it.isNotBlank() }
        .doOnNext {
          view.moveToNextView(4)
        }
        .subscribe({}, { it.printStackTrace() }))

    disposables.add(view.getFifthChar()
        .filter { it.isNotBlank() }
        .doOnNext {
          view.moveToNextView(5)
        }
        .subscribe({}, { it.printStackTrace() }))

    disposables.add(view.getSixthChar()
        .filter { it.isNotBlank() }
        .doOnNext {
          view.hideKeyboard()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.dispose()
  }

  fun onResume(code: Code?) {
    if (!isLastStep) {
      code?.let { view.setCode(code) }
      view.focusAndShowKeyboard()
    }
  }
}