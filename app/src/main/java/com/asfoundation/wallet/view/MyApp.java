package com.asfoundation.wallet.view;

import android.app.Application;
import android.support.annotation.NonNull;
import com.asfoundation.wallet.billing.payment.Adyen;
import com.jakewharton.rxrelay.PublishRelay;
import java.nio.charset.Charset;
import rx.schedulers.Schedulers;

public class MyApp extends Application {

  private Adyen adyen;

  @Override public void onCreate() {
    super.onCreate();

    this.adyen = getAdyen();
  }

  public @NonNull Adyen getAdyen() {
    if (adyen == null) {
      adyen = new Adyen(this, Charset.forName("UTF-8"), Schedulers.io(), PublishRelay.create());
    }
    return adyen;
  }
}
