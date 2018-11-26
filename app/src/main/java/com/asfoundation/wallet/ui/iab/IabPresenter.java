package com.asfoundation.wallet.ui.iab;

import android.os.Bundle;

/**
 * Created by franciscocalado on 20/07/2018.
 */

public class IabPresenter {

  private final IabView view;

  public IabPresenter(IabView view) {
    this.view = view;
  }

  public void present(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      view.showPaymentMethodsView();
    }
  }
}