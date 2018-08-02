package com.asfoundation.wallet.billing.view.card;

import android.support.v4.app.FragmentManager;
import com.asf.wallet.R;
import com.asfoundation.wallet.view.fragment.GreenFragment;
import com.asfoundation.wallet.view.fragment.RedFragment;

public class CreditCardFragmentNavigator implements CreditCardNavigator {

  private final FragmentManager fragmentManager;

  public CreditCardFragmentNavigator(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  @Override public void popView() {
    fragmentManager.beginTransaction()
        .add(R.id.fragment_container, new GreenFragment())
        .commit();
  }

  @Override public void popViewWithError() {
    popView();

    fragmentManager.beginTransaction()
        .add(R.id.fragment_container, new RedFragment())
        .commit();
  }
}
