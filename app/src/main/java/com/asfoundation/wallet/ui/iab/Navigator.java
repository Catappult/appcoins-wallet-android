package com.asfoundation.wallet.ui.iab;

import android.net.Uri;
import android.os.Bundle;
import io.reactivex.Observable;

public interface Navigator {

  void popView(Bundle bundle);

  void popViewWithError();

  void navigateToUriForResult(String redirectUrl);

  Observable<Uri> uriResults();

  void navigateBack();
}
