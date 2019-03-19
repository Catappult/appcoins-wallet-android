package com.asfoundation.wallet.navigator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Observable;
import java.math.BigDecimal;

public interface UriNavigator {

  void navigateToUri(String url, String domain, String skuId, BigDecimal amount, String type);

  Observable<Uri> uriResults();

  Intent getActivityIntent(String url);

}
