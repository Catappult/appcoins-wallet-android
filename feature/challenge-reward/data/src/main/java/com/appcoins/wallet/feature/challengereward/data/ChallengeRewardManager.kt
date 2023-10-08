package com.appcoins.wallet.feature.challengereward.data

import android.app.Activity
import com.appcoins.wallet.feature.challengereward.data.model.ChallengeRewardListener
import com.fyber.fairbid.ads.OfferWall
import com.fyber.fairbid.ads.offerwall.ShowOptions

const val APP_ID = "135904"
const val PLACEMENT_ID = "Wallet_Placement"

object ChallengeRewardManager {

  fun create(activity: Activity, walletAddress: String) {
    OfferWall.userId = walletAddress
    OfferWall.start(
      activity = activity,
      appId = APP_ID,
      offerWallListener = ChallengeRewardListener(),
      disableAdvertisingId = false
    )
  }

  fun onNavigate() {
    val customParameters = mutableMapOf("key" to "value") // TODO Personalise Parameters
    val showOptions = ShowOptions(customParams = customParameters)
    OfferWall.show(
      showOptions = showOptions,
      placementId = PLACEMENT_ID
    )
  }
}
