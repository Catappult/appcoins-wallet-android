package com.asfoundation.wallet.wallet_validation

import com.asf.wallet.R
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.repository.SmsValidationRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
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
            .subscribe(
                {
                  when (it) {
                    SmsValidationRepository.Status.VERIFIED -> activity?.showSuccess()
                    else -> handleError(null)
                  }
                },
                {
                  handleError(it)
                }
            )
    )
  }

  private fun handleError(throwable: Throwable?) {
    var errorMessage: Int = R.string.unknown_error

    if (throwable is HttpException) {
      errorMessage = when (throwable.code()) {
        400 -> R.string.wallet_validation_code_invalid
        429 -> R.string.wallet_validation_many_requests
        else -> R.string.unknown_error
      }
    }
    activity?.showCodeValidationView(validationInfo, errorMessage)
  }

  fun stop() {
    disposables.clear()
    view.clean()
  }

}
