package com.asfoundation.wallet.wallets.db

import androidx.room.*
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity
import com.asfoundation.wallet.wallets.db.entity.WalletInfoUpdate
import com.asfoundation.wallet.wallets.db.entity.WalletInfoUpdateName
import com.asfoundation.wallet.wallets.db.entity.WalletInfoUpdateWithBalance
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

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress LIMIT 1")
  fun observeWalletInfo(walletAddress: String): Observable<WalletInfoEntity>

  @Query("SELECT * FROM WalletInfoEntity WHERE wallet = :walletAddress LIMIT 1")
  fun getWalletInfo(walletAddress: String): Single<List<WalletInfoEntity>>
}