package com.asfoundation.wallet.ui.onboarding;

import io.reactivex.Observable;

public interface OnboardingView {

  Observable<Object> getOkClick();

  Observable<Object> getSkipClick();

  Observable<Object> getCheckboxClick();
}
