package com.asfoundation.wallet.wallets.db

import androidx.room.*
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import com.asfoundation.wallet.wallets.db.entity.WalletInfoUpdate
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface WalletInfoDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertWalletInfoWithFiat(walletInfo: WalletInfoEntity)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertWalletInfo(walletInfo: WalletInfoEntity)

  @Update(entity = WalletInfoEntity::class)
  fun updateWalletInfo(walletInfoUpdate: WalletInfoUpdate)

  /**
   * Attempts to insert a WalletInfoEntity without fiat values. If it already exists, it just
   * updates the relevant fields.
   */
  @Transaction
  fun insertOrUpdateNoFiat(walletInfo: WalletInfoEntity) {
    try {
      insertWalletInfo(walletInfo)
    } catch (e: Exception) {
      updateWalletInfo(
        WalletInfoUpdate(
          walletInfo.wallet, walletInfo.appcCreditsBalanceWei,
          walletInfo.appcBalanceWei, walletInfo.ethBalanceWei, walletInfo.blocked,
          walletInfo.verified, walletInfo.logging
        )
      )
    }
  }

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress LIMIT 1")
  fun observeWalletInfo(walletAddress: String): Observable<WalletInfoEntity>

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress LIMIT 1")
  fun getWalletInfo(walletAddress: String): Single<List<WalletInfoEntity>>
}