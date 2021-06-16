package com.appcoins.wallet.gamification.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.appcoins.wallet.gamification.repository.entity.WalletOriginEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface WalletOriginDao {

  @Query("select * from WalletOriginEntity where wallet_address = :wallet")
  fun getWalletOrigin(wallet: String): Single<WalletOriginEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertWalletOrigin(walletOriginEntity: WalletOriginEntity): Completable
}
