package com.asfoundation.wallet.wallets.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import io.reactivex.Completable
import io.reactivex.Observable

@Dao
interface WalletInfoDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertWalletInfo(walletInfo: WalletInfoEntity): Completable

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress")
  fun observeWalletInfo(walletAddress: String): Observable<List<WalletInfoEntity>>
}