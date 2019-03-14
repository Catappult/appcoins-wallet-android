package com.asfoundation.wallet.navigator;

import android.content.Intent;
import android.net.Uri;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;

public interface UriNavigator {

  void navigateToUri(String url, TransactionBuilder transaction);

  Observable<Uri> uriResults();

  Intent getActivityIntent();

}
