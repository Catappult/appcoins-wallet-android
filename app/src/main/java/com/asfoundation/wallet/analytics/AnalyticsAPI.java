package com.asfoundation.wallet.analytics;

import cm.aptoide.analytics.AnalyticsManager;
import io.reactivex.Completable;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AnalyticsAPI {

  @POST("user/addEvent/action={action}/context=WALLET/name={name}")
  Completable registerEvent(
      @Path("action") AnalyticsManager.Action action, @Path("name") String eventName, @Body AnalyticsBody body);
}
