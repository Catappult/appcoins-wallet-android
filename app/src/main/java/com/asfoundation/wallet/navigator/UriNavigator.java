package com.asfoundation.wallet.navigator;

import android.net.Uri;
import io.reactivex.Observable;

public interface UriNavigator {

  void navigateToUri(String url);

  Observable<Uri> uriResults();
}
