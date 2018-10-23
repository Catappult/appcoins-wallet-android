package com.asfoundation.wallet.service

import com.asfoundation.wallet.apps.repository.webservice.data.Application
import io.reactivex.Single
import retrofit2.http.GET


interface AppsApi {
  companion object{
    const val API_BASE_URL = "https://ws75.aptoide.com/api/7/"
  }

  @GET("listApps/store_name=bds-store/group_id=10358961/limit=10/order=rand")
  fun getApplications(): Single<Application>?
}