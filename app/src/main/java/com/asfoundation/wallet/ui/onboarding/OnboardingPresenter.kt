package com.asfoundation.wallet.ui.onboarding

import io.reactivex.disposables.CompositeDisposable

class OnboardingPresenter(val disposables: CompositeDisposable,
                          val view: OnboardingFragment) {

  fun present() {
    handleOkClick()
    handleSkipClick()
    handleCheckboxClick()
    handlePageScroll()
  }

  private fun handleOkClick() {
    disposables.add(view.getOkClick().subscribe())
  }

  private fun handleSkipClick() {
    disposables.add(view.getSkipClick().subscribe())
  }

  private fun handleCheckboxClick() {
    disposables.add(view.getCheckboxClick().subscribe())
  }

  private fun handlePageScroll() {

  }
}