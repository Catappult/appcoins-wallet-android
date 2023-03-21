package com.asfoundation.wallet.subscriptions.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appcoins.wallet.core.network.microservices.model.SubscriptionSubStatus
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface UserSubscriptionsDao {

  @Query("select * from UserSubscriptionEntity where wallet_address like :walletAddress")
  fun getSubscriptions(walletAddress: String): Single<List<UserSubscriptionEntity>>

  @Query(
      "select * from UserSubscriptionEntity where wallet_address like :walletAddress and sub_status like :subStatus")
  fun getSubscriptionsByStatus(walletAddress: String,
                               subStatus: SubscriptionSubStatus): Single<List<UserSubscriptionEntity>>

  @Query(
      "select * from UserSubscriptionEntity where wallet_address like :walletAddress and sub_status like :subStatus LIMIT :limit")
  fun getSubscriptionsBySubStatusWithLimit(walletAddress: String,
                                           subStatus: SubscriptionSubStatus,
                                           limit: Int): Single<List<UserSubscriptionEntity>>

  @Query(
      "select * from UserSubscriptionEntity where wallet_address like :walletAddress LIMIT :limit")
  fun getSubscriptionsWithLimit(walletAddress: String,
                                limit: Int): Single<List<UserSubscriptionEntity>>

  @Query("DELETE FROM UserSubscriptionEntity")
  fun deleteSubscriptions(): Completable

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertSubscriptions(userSubscriptions: List<UserSubscriptionEntity>)
}
