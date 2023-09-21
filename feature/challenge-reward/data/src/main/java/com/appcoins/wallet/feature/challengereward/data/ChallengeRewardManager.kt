package com.appcoins.wallet.feature.challengereward.data

import android.app.Activity
import com.appcoins.wallet.feature.challengereward.data.model.ChallengeRewardListener
import com.fyber.fairbid.ads.OfferWall

const val PLACEMENT_ID = "Wallet_Placement"

object ChallengeRewardManager {

  fun create(appId: String, activity: Activity) {
    OfferWall.userId = "userId" // TODO Provide User ID
    OfferWall.start(
      appId = appId,
      activity = activity,
      offerWallListener = ChallengeRewardListener(),
      disableAdvertisingId = true,
    )
  }

  fun onNavigate() = OfferWall.show(placementId = PLACEMENT_ID)
}
