package com.asfoundation.wallet.referrals

import io.reactivex.Completable
import io.reactivex.Single

interface ReferralLocalData {
  fun saveReferralInformation(address: String, totalEarned: String, invitedFriends: Int,
                              isVerified: Boolean, screen: String): Completable

  fun getReferralInformation(address: String, screen: String): Single<String>
}