package com.appcoins.wallet.feature.walletInfo.data.wallet.db

import androidx.room.*
import com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity.WalletInfoDelete
import com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity.WalletInfoEntity
import com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity.WalletInfoUpdate
import com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity.WalletInfoUpdateName
import com.appcoins.wallet.feature.walletInfo.data.wallet.db.entity.WalletInfoUpdateWithBalance
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface WalletInfoDao {
  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertWalletInfo(walletInfo: WalletInfoEntity)

  @Update(entity = WalletInfoEntity::class)
  fun updateWalletInfo(walletInfoUpdate: WalletInfoUpdate)

  @Update(entity = WalletInfoEntity::class)
  fun updateWalletInfo(walletInfoUpdateWithBalance: WalletInfoUpdateWithBalance)

  @Update(entity = WalletInfoEntity::class)
  fun updateWalletInfo(walletInfoUpdateName: WalletInfoUpdateName)

  @Delete(entity = WalletInfoEntity::class)
  fun deleteWalletInfo(walletInfoDelete: WalletInfoDelete)

  /**
   * Attempts to insert a WalletInfoEntity without fiat values. If it already exists, it just
   * updates the relevant fields.
   */
  fun insertOrUpdateNoFiat(walletInfo: WalletInfoEntity) {
    return try {
      insertWalletInfo(walletInfo)
    } catch (e: Exception) {
      updateWalletInfo(
        WalletInfoUpdate(
          wallet = walletInfo.wallet,
          appcCreditsBalanceWei = walletInfo.appcCreditsBalanceWei,
          appcBalanceWei = walletInfo.appcBalanceWei,
          ethBalanceWei = walletInfo.ethBalanceWei,
          blocked = walletInfo.blocked,
          verified = walletInfo.verified,
          logging = walletInfo.logging,
          hasBackup = walletInfo.hasBackup
        )
      )
    }
  }

  /**
   * Attempts to insert a WalletInfoEntity with fiat values. If it already exists, it just
   * updates the relevant fields.
   */
  fun insertOrUpdateWithFiat(walletInfo: WalletInfoEntity) {
    try {
      insertWalletInfo(walletInfo)
    } catch (e: Exception) {
      updateWalletInfo(
        WalletInfoUpdateWithBalance(
          wallet = walletInfo.wallet,
          appcCreditsBalanceWei = walletInfo.appcCreditsBalanceWei,
          appcBalanceWei = walletInfo.appcBalanceWei,
          ethBalanceWei = walletInfo.ethBalanceWei,
          blocked = walletInfo.blocked,
          verified = walletInfo.verified,
          logging = walletInfo.logging,
          hasBackup = walletInfo.hasBackup,
          appcCreditsBalanceFiat = walletInfo.appcCreditsBalanceFiat,
          appcBalanceFiat = walletInfo.appcBalanceFiat,
          ethBalanceFiat = walletInfo.ethBalanceFiat,
          fiatCurrency = walletInfo.fiatCurrency,
          fiatSymbol = walletInfo.fiatSymbol,
        )
      )
    }
  }

  /**
   * Attempts to insert a WalletInfoEntity with fiat values. If it already exists, it just
   * updates the relevant fields.
   */
  fun insertOrUpdateName(walletInfo: WalletInfoEntity) {
    try {
      insertWalletInfo(walletInfo)
    } catch (e: Exception) {
      updateWalletInfo(
        WalletInfoUpdateName(
          wallet = walletInfo.wallet,
          name = walletInfo.name
        )
      )
    }
  }

  @Transaction
  fun deleteByAddress(walletAddress: String) = deleteWalletInfo(WalletInfoDelete(walletAddress))

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress LIMIT 1")
  fun observeWalletInfo(walletAddress: String): Observable<WalletInfoEntity>

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress LIMIT 1")
  fun getWalletInfo(walletAddress: String): Single<List<WalletInfoEntity>>
}