package com.asfoundation.wallet.referrals

import io.reactivex.Completable
import io.reactivex.Single

interface ReferralLocalData {
  fun saveTotalEarned(address: String, totalEarned: String, screen: String): Completable
  fun saveNumberOfFriends(address: String, numberOfFriends: Int, screen: String): Completable
  fun getNumberOfFriends(address: String, screen: String): Single<Int>
  fun getEarned(address: String, screen: String): Single<String>

}