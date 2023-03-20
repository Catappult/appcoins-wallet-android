package com.appcoins.wallet.core.network.analytics;

import cm.aptoide.analytics.AnalyticsManager.Action;
import io.reactivex.Completable;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AnalyticsAPI {

  @POST("user/addEvent/action={action}/context=WALLET/name={name}") Completable registerEvent(
      @Path("action") Action action, @Path("name") String eventName, @Body AnalyticsBody body);
}
