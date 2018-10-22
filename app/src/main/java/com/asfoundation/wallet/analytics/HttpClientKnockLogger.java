package com.asfoundation.wallet.analytics;

import cm.aptoide.analytics.KnockEventLogger;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpClientKnockLogger implements KnockEventLogger {
  private final OkHttpClient okHttpClient;

  public HttpClientKnockLogger(OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
  }

  @Override public void log(String url) {
    Request request = new Request.Builder().url(url)
        .build();
    try {
      okHttpClient.newCall(request)
          .execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
