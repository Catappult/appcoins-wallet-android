package com.asfoundation.wallet.repository

import android.util.Log
import com.appcoins.wallet.core.network.backend.api.GamesApi
import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = GamesRepositoryType::class)
class GamesRepository @Inject constructor(private val gamesApi: GamesApi) :
  GamesRepositoryType {

  private val defaultBackground = "https://image.winudf.com/v2/image1/Y29tLm5hdGhuZXR3b3JrLnVsdHJhcHJvX3NjcmVlbl8xXzE2MzE3MzE3MTlfMDQ5/screen-1.webp?fakeurl=1&type=.webp"

  override fun getGamesListing(): Single<List<GameData>> {
    return gamesApi.getGamesListing()
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

}
