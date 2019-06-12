package com.asfoundation.wallet.wallet_validation

import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class ValidationLoadingPresenter(
    private val view: ValidationLoadingFragmentView,
    private val activity: WalletValidationActivityView?,
    private val defaultWalletInteract: FindDefaultWalletInteract,
    private val smsValidationInteract: SmsValidationInteract,
    private val validationInfo: ValidationInfo,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler
) {

  private val disposables: CompositeDisposable = CompositeDisposable()

  fun present() {
    view.show()

    handleValidationWallet()
  }

  private fun handleValidationWallet() {
    disposables.add(
        defaultWalletInteract.find()
            .delay(1, TimeUnit.SECONDS)
            .flatMap { wallet ->
              smsValidationInteract.validateCode(
                  "+${validationInfo.countryCode}${validationInfo.phoneNumber}",
                  wallet,
                  "${validationInfo.code1}${validationInfo.code2}${validationInfo.code3}${validationInfo.code4}${validationInfo.code5}${validationInfo.code6}")
            }
            .doOnSubscribe { view.show() }
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe { status ->
              handleNext(status)
            }
    )
  }

  private fun handleNext(status: WalletValidationStatus) {
    when (status) {
      WalletValidationStatus.SUCCESS -> activity?.showSuccess()
      WalletValidationStatus.INVALID_INPUT,
      WalletValidationStatus.INVALID_PHONE -> handleError(
          R.string.wallet_validation_phone_number_invalid)
      WalletValidationStatus.DOUBLE_SPENT -> handleError(
          R.string.wallet_validation_used_phone)
      WalletValidationStatus.GENERIC_ERROR -> handleError(R.string.unknown_error)
    }
  }

  private fun handleError(errorMessage: Int) {
    activity?.showCodeValidationView(validationInfo, errorMessage)
  }

  fun stop() {
    disposables.clear()
    view.clean()
  }

}
