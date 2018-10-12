package com.asfoundation.wallet.service

import com.asfoundation.wallet.apps.ApplicationsApi
import com.asfoundation.wallet.apps.repository.webservice.data.Application
import io.reactivex.Single

class BDSAppsApi(val api: AppsApi) : ApplicationsApi {
  override fun getApplications(): Single<Application>? {
    return api.getApplications()
  }
}