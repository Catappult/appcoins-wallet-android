package com.asfoundation.wallet.wallet_verification.intro

import android.os.Bundle
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationIntroPresenter(private val view: WalletVerificationIntroView,
                                       private val disposable: CompositeDisposable,
                                       private val navigator: WalletVerificationIntroNavigator,
                                       private val logger: Logger,
                                       private val viewScheduler: Scheduler,
                                       private val ioScheduler: Scheduler,
                                       private val interactor: WalletVerificationIntroInteractor) {

  companion object {

    private val TAG = WalletVerificationIntroPresenter::class.java.name
  }

  fun present(savedInstanceState: Bundle?) {
    loadModel(savedInstanceState)
    handleCancelClicks()
    handleSubmitClicks()
    handleForgetCardClick()
  }

  private fun loadModel(savedInstanceState: Bundle?) {
    disposable.add(
        interactor.loadVerificationIntroModel()
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.updateUi(it)
              view.finishCardConfiguration(it.paymentInfoModel.paymentMethodInfo!!,
                  it.paymentInfoModel.isStored, false, savedInstanceState)
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleCancelClicks() {
    disposable.add(
        view.getCancelClicks()
            .doOnNext { view.cancel() }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleSubmitClicks() {
    disposable.add(
        view.getSubmitClicks()
            .doOnNext { navigator.navigateToCodeView() }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleForgetCardClick() {
    disposable.add(view.forgetCardClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showLoading() }
        .observeOn(ioScheduler)
        .flatMapSingle { interactor.disablePayments() }
        .observeOn(viewScheduler)
        .doOnNext { success -> if (!success) view.showGenericError() }
        .filter { it }
        .observeOn(ioScheduler)
        .flatMapSingle {
          interactor.loadVerificationIntroModel()
              .doOnSuccess {
                view.updateUi(it)
                view.finishCardConfiguration(it.paymentInfoModel.paymentMethodInfo!!,
                    it.paymentInfoModel.isStored, false, null)
              }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          view.showGenericError()
        }))
  }

  fun stop() = disposable.clear()
}
