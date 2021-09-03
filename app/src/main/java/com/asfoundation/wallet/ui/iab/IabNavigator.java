package com.asfoundation.wallet.ui.iab;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import com.asfoundation.wallet.navigator.UriNavigator;
import io.reactivex.Observable;

public class IabNavigator implements Navigator {

  private FragmentManager fragmentManager;
  private final UriNavigator uriNavigator;
  private final IabView iabView;

  public IabNavigator(FragmentManager fragmentManager, UriNavigator uriNavigator, IabView iabView) {
    this.fragmentManager = fragmentManager;
    this.uriNavigator = uriNavigator;
    this.iabView = iabView;
  }

  @Override public void popView(Bundle bundle) {
    iabView.finish(bundle);
  }

  @Override public void popViewWithError() {
    iabView.close(new Bundle());
  }

  @Override public void navigateToUriForResult(String redirectUrl) {
    uriNavigator.navigateToUri(redirectUrl);
  }

  @Override public Observable<Uri> uriResults() {
    return uriNavigator.uriResults();
  }

  //Not used since that would lead to a bigger refactor, but overrided so that it can be
  // implemented on topup
  @Override public void navigateBack() {
    if (fragmentManager.getBackStackEntryCount() != 0) {
      fragmentManager.popBackStack();
    } else {
      iabView.close(null);
    }
  }
}
