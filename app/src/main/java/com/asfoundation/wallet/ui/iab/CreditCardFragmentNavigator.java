package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import static com.asfoundation.wallet.ui.iab.ExpressCheckoutBuyFragment.PAYMENT_METHOD_CC;

public class CreditCardFragmentNavigator implements CreditCardNavigator {

  private final FragmentManager fragmentManager;
  private final IabView iabView;

  public CreditCardFragmentNavigator(FragmentManager fragmentManager, IabView iabView) {
    this.fragmentManager = fragmentManager;
    this.iabView = iabView;
  }

  @Override public void popView(Bundle bundle) {
    iabView.finish(bundle);
  }

  @Override public void popViewWithError() {
    iabView.close(new Bundle());
  }
}
