package com.asfoundation.wallet.wallets.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.asfoundation.wallet.wallets.db.entity.WalletInfoEntity

@Database(entities = [WalletInfoEntity::class], version = 1)
abstract class WalletInfoDatabase : RoomDatabase() {
  abstract fun walletInfoDao(): WalletInfoDao

}