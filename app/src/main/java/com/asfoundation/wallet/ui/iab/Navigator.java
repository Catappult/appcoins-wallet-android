package com.asfoundation.wallet.ui.iab;

import android.net.Uri;
import android.os.Bundle;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;

public interface Navigator {

  void popView(Bundle bundle);

  void popViewWithError();

  void navigateToUriForResult(String redirectUrl, String transactionUid,
      TransactionBuilder transaction);

  Observable<Uri> uriResults();
}
