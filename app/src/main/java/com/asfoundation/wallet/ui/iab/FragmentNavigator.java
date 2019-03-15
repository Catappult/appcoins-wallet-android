package com.asfoundation.wallet.ui.iab;

import android.net.Uri;
import android.os.Bundle;
import com.asfoundation.wallet.entity.TransactionBuilder;
import com.asfoundation.wallet.navigator.UriNavigator;
import io.reactivex.Observable;
import java.math.BigDecimal;

public class FragmentNavigator implements Navigator {

  private final UriNavigator uriNavigator;
  private final IabView iabView;

  public FragmentNavigator(UriNavigator uriNavigator, IabView iabView) {
    this.uriNavigator = uriNavigator;
    this.iabView = iabView;
  }

  @Override public void popView(Bundle bundle) {
    iabView.finish(bundle);
  }

  @Override public void popViewWithError() {
    iabView.close(new Bundle());
  }

  @Override public void navigateToUriForResult(String redirectUrl, String transactionUid,
      String domain, String skuId, BigDecimal amount, String type) {
    uriNavigator.navigateToUri(redirectUrl, domain, skuId,
        amount, type);
  }

  @Override public Observable<Uri> uriResults() {
    return uriNavigator.uriResults();
  }
}
