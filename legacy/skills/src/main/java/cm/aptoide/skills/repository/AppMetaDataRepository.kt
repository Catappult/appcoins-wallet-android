package cm.aptoide.skills.repository

import com.appcoins.wallet.core.network.eskills.api.AppDataApi
import com.appcoins.wallet.core.network.eskills.model.AppInfo
import io.reactivex.Single
import javax.inject.Inject

class AppMetaDataRepository @Inject constructor(private val appDataApi: AppDataApi)  {

  fun getMeta(
    packageName: String
  ): Single<AppInfo> {
    return appDataApi.getMeta(packageName)
  }

}