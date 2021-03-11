package com.asfoundation.wallet.ui.iab;

import android.net.Uri;
import android.os.Bundle;
import io.reactivex.Observable;

public interface Navigator {

  void finishPayment(Bundle bundle);

  void finishPaymentWithError();

  void navigateToUriForResult(String redirectUrl);

  Observable<Uri> uriResults();

  void navigateBack();
}
