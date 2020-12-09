package com.asfoundation.wallet.abtesting

import com.asf.wallet.BuildConfig
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class ABTestApiProvider(private val httpClient: OkHttpClient,
                        private val converterFactory: Converter.Factory,
                        private val rxCallAdapterFactory: CallAdapter.Factory) {

  fun getApi(type: BaseExperiment.ExperimentType?): ABTestApi {
    if (type == BaseExperiment.ExperimentType.RAKAM) {
      return getABTestApi(BuildConfig.APTOIDE_WEB_SERVICES_AB_TEST_HOST)
    } else if (type == BaseExperiment.ExperimentType.WASABI) {
      return getABTestApi(BuildConfig.APTOIDE_WEB_SERVICES_AB_TESTING_HOST)
    }
    throw IllegalStateException(
        "You need to pass a valid ExperimentType! All experiments must be assigned to an Experiment type so that the base host can be correctly assigned")
  }

  private fun getABTestApi(baseHost: String): ABTestApi {
    return createRetrofit(baseHost).create(ABTestApi::class.java)
  }

  private fun createRetrofit(baseHost: String): Retrofit {
    return Retrofit.Builder()
        .baseUrl(baseHost)
        .client(httpClient)
        .addCallAdapterFactory(rxCallAdapterFactory)
        .addConverterFactory(converterFactory)
        .build()
  }
}

interface ABTestApi {
  @GET("assignments/applications/Android/experiments/{experimentName}/users/{aptoideId}")
  fun getExperiment(@Path(value = "experimentName") experimentName: String,
                    @Path(value = "aptoideId")
                    aptoideId: String): Observable<ABTestImpressionResponse>

  @POST("events/applications/Android/experiments/{experimentName}/users/{aptoideId}")
  fun recordImpression(@Path(value = "experimentName") experimentName: String,
                       @Path(value = "aptoideId") aptoideId: String,
                       @Body body: ABTestRequestBody): Completable

  @POST("events/applications/Android/experiments/{experimentName}/users/{aptoideId}")
  fun recordAction(@Path(value = "experimentName") experimentName: String,
                   @Path(value = "aptoideId") aptoideId: String,
                   @Body body: ABTestRequestBody): Completable
}

