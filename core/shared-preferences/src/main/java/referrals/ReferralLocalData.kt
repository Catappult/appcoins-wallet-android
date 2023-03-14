package referrals

import io.reactivex.Completable
import io.reactivex.Single

interface ReferralLocalData {
  fun saveReferralInformation(address: String, invitedFriends: Int,
                              isVerified: Boolean, screen: String): Completable

  fun getReferralInformation(address: String, screen: String): Single<String>

  fun savePendingAmountNotification(address: String, pendingAmount: String): Completable

  fun getPendingAmountNotification(address: String): Single<String>
}