package com.asfoundation.wallet.billing.view.card;

import android.support.v4.app.FragmentManager;
import com.asf.wallet.R;
import com.asfoundation.wallet.ui.iab.IabView;
import com.asfoundation.wallet.view.fragment.RedFragment;

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
    fragmentManager.beginTransaction()
        .add(R.id.fragment_container, new RedFragment())
        .commit();
  }
}
