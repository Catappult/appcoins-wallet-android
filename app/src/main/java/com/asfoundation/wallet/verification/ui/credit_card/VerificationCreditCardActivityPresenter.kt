package com.asfoundation.wallet.verification.ui.credit_card

import android.os.Bundle
import com.appcoins.wallet.core.utils.common.RxSchedulers
import com.asfoundation.wallet.verification.ui.credit_card.code.VerificationCodeFragment
import com.asfoundation.wallet.verification.ui.credit_card.error.VerificationErrorFragment
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import io.reactivex.disposables.CompositeDisposable

class VerificationCreditCardActivityPresenter(
  private val view: VerificationCreditCardActivityView,
  private val navigator: VerificationCreditCardActivityNavigator,
  private val interactor: VerificationCreditCardActivityInteractor,
  private val rxSchedulers: RxSchedulers,
  private val disposable: CompositeDisposable,
  private val analytics: VerificationAnalytics
) {

  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) handleVerificationStatus()
    handleToolbarBackPressEvents()
  }

  private fun handleToolbarBackPressEvents() {
    disposable.add(
        view.getToolbarBackPressEvents()
            .doOnNext { fragmentName ->
              if (fragmentName == VerificationErrorFragment::class.java.name ||
                  fragmentName == VerificationCodeFragment::class.java.name) {
                navigator.navigateToWalletVerificationIntroNoStack()
              } else {
                navigator.backPress()
              }
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleVerificationStatus() {
    disposable.add(
      interactor.getVerificationStatus()
        .subscribeOn(rxSchedulers.io)
        .observeOn(rxSchedulers.main)
        .doOnSuccess { onVerificationStatusSuccess(it) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun onVerificationStatusSuccess(verificationStatus: VerificationStatus) {
    when (verificationStatus) {
      VerificationStatus.UNVERIFIED -> {
        analytics.sendStartEvent("verify")
        navigator.navigateToWalletVerificationIntro()
      }
      VerificationStatus.CODE_REQUESTED -> {
        analytics.sendStartEvent("insert_code")
        navigator.navigateToWalletVerificationCode()
      }
      else -> navigator.finish()
    }
  }
}