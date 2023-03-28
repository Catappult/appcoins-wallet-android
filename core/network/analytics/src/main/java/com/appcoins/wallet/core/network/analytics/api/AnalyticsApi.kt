package com.appcoins.wallet.core.network.analytics.api

import cm.aptoide.analytics.AnalyticsManager
import com.appcoins.wallet.core.network.analytics.AnalyticsBody
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AnalyticsApi {
  @POST("user/addEvent/action={action}/context=WALLET/name={name}")
  fun registerEvent(
    @Path("action") action: AnalyticsManager.Action?,
    @Path("name") eventName: String?,
    @Body body: AnalyticsBody?
  ): Completable
}