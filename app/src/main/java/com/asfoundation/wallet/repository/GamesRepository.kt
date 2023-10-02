package com.asfoundation.wallet.repository

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
  private val appDataApi: AppDataApi
) :
  GamesRepositoryType {

  object ApiParameters {
    const val GROUP = "e-skills"
    const val LIMIT = "20"
    const val SORT = "downloads7d"
    const val LANGUAGE = "en"
    const val STORE = "apps"
  }

  override fun getGamesListing(): Single<List<GameData>> {
    return gamesApi.getGamesListing(
      group = ApiParameters.GROUP,
      limit = ApiParameters.LIMIT,
      sort = ApiParameters.SORT,
      language = ApiParameters.LANGUAGE,
      store = ApiParameters.STORE
    )
      .map {
        it.dataList.list.map {
          GameData(
            title = it.appName,
            gameIcon = it.appIcon,
            gameBackground = it.background,
            gamePackage = it.packageName
          )

        }

      }
  }

  override fun getGameDetails(packageName: String): Single<GameDetailsData> {
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
              gameBackground = it.data.background,
              gamePackage = it.data.packageName,
              description = it.data.media.description,
              screenshots = it1,
              rating = it.data.stats.rating.avg,
              downloads = it.data.stats.downloads,
              size = it.data.size,
              md5 = it.data.file.md5,
              url = it.data.file.path,
              version = it.data.file.versionCode

            )
          }
      }
  }

}
