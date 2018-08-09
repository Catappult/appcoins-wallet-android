package com.asfoundation.wallet.ui.iab;

import android.support.v4.app.FragmentManager;

public class CreditCardFragmentNavigator implements CreditCardNavigator {

  private final FragmentManager fragmentManager;
  private final IabView iabView;

  public CreditCardFragmentNavigator(FragmentManager fragmentManager, IabView iabView) {
    this.fragmentManager = fragmentManager;
    this.iabView = iabView;
  }

  @Override public void popView(String transactionUid) {
    iabView.finish(transactionUid);
  }

  @Override public void popViewWithError() {
    iabView.showError();
  }
}
