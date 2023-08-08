package com.asfoundation.wallet.repository

import android.util.Log
import com.appcoins.wallet.core.network.backend.api.GamesApi
import com.appcoins.wallet.core.network.eskills.api.AppDataApi
import com.appcoins.wallet.core.network.eskills.api.EskillsGamesApi
import com.appcoins.wallet.ui.widgets.GameData
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.appcoins.wallet.ui.widgets.Screenshot
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = GamesRepositoryType::class)
class GamesRepository @Inject constructor(
  private val gamesApi: EskillsGamesApi,
  private val appDataApi: AppDataApi) :
  GamesRepositoryType {

  private val defaultBackground = "https://image.winudf.com/v2/image1/Y29tLm5hdGhuZXR3b3JrLnVsdHJhcHJvX3NjcmVlbl8xXzE2MzE3MzE3MTlfMDQ5/screen-1.webp?fakeurl=1&type=.webp"

  override fun getGamesListing(): Single<List<GameData>> {
    return gamesApi.getGamesListing(
      group = "e-skills",
      limit = "20",
      sort = "downloads7d",
      language = "en",
      store = "apps"
    )
      .map { it.dataList.list.map {
        Log.d("App Name","Name: "+it.appName)
        Log.d("App Icon","Icon: "+it.appIcon)
        Log.d("App Background","Background: "+it.background)
        Log.d("App Pack","Package: "+it.packageName)
        GameData(
          title = it.appName,
          gameIcon = it.appIcon,
          gameBackground = if (it.background == null) defaultBackground else it.background,
          gamePackage = it.packageName
        )

      }

      }
  }

  override fun getGameDetails(packageName:String): Single<GameDetailsData> {
    return appDataApi.getMeta(packageName)
      .map { it ->
        it.data.media.screenshots
          ?.map {
            Screenshot(
                    it.imageUrl,
                    it.height,
                    it.width
            )
          }?.let { it1 ->
            GameDetailsData(
              title = it.data.name,
              gameIcon = it.data.appIcon,
              gameBackground = if (it.data.background == null) defaultBackground else it.data.background,
              gamePackage = it.data.packageName,
              description = it.data.media.description,
              screenshots = it1,
              size = it.data.size,
              rating = it.data.stats.rating.avg,
              downloads = it.data.stats.downloads
            )
          }
      }
  }

}
