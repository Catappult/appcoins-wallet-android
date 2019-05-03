package com.asfoundation.wallet.ui.onboarding;

import io.reactivex.disposables.CompositeDisposable;

public class OnboardingPresenter {

  private final CompositeDisposable disposables;
  private OnboardingView view;

  public OnboardingPresenter(CompositeDisposable disposables, OnboardingView view) {
    this.disposables = disposables;
    this.view = view;
  }

  public void present() {
    handleOkClick();
    handleSkipClick();
    handleCheckboxClick();
    handlePageScroll();
  }

  private void handleOkClick() {
    disposables.add(view.getOkClick().subscribe());
  }

  private void handleSkipClick() {
    disposables.add(view.getSkipClick().subscribe());
  }

  private void handleCheckboxClick() {
    disposables.add(view.getCheckboxClick().subscribe());
  }

  private void handlePageScroll() {

  }
}