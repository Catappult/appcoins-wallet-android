package com.asfoundation.wallet.repository

import io.reactivex.Completable
import io.reactivex.Single

interface PreferencesRepositoryType {

  fun getCurrentWalletAddress(): String?

  fun setCurrentWalletAddress(address: String)

  fun getAndroidId(): String

  fun setAndroidId(androidId: String)

  fun getWalletPurchasesCount(walletAddress: String): Int

  fun incrementWalletPurchasesCount(walletAddress: String, count: Int): Completable

  fun setWalletId(walletId: String)

  fun getWalletId(): String?
}
