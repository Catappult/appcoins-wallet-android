package com.appcoins.wallet.feature.challengereward.data.model

import android.util.Log
import com.fyber.fairbid.ads.OfferWall
import com.fyber.fairbid.ads.offerwall.OfferWallError
import com.fyber.fairbid.ads.offerwall.OfferWallListener

class ChallengeRewardListener : OfferWallListener {
  override fun onClose(placementId: String?) {
    Log.i("ChallengesListener", "Challenges closed! placement id: $placementId ${OfferWall.userId}")
    // TODO("Not yet implemented")
  }

  override fun onShow(placementId: String?) {
    Log.i("ChallengesListener", "Challenges shown! placement id: $placementId ${OfferWall.userId}")
    // TODO("Not yet implemented")
  }

  override fun onShowError(
    placementId: String?,
    error: OfferWallError,
  ) {
    Log.i("ChallengesListener", "Challenges shows error: $error")
    // TODO("Not yet implemented")
  }
}
