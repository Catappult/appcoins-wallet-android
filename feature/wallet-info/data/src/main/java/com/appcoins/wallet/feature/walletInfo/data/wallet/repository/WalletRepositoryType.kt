package com.appcoins.wallet.feature.walletInfo.data.wallet.repository
import com.appcoins.wallet.feature.walletInfo.data.RestoreResult
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface WalletRepositoryType {

  fun fetchWallets(): Single<Array<Wallet>>

  fun findWallet(address: String): Single<Wallet>

  fun createWallet(password: String): Single<Wallet>

  fun restoreKeystoreToWallet(store: String, password: String,
                              newPassword: String): Single<RestoreResult>

  fun restorePrivateKeyToWallet(privateKey: String?, newPassword: String): Single<RestoreResult>

  fun exportWallet(address: String, password: String, newPassword: String?): Single<String>

  fun deleteWallet(address: String, password: String): Completable

  fun setDefaultWallet(address: String): Completable

  fun getDefaultWallet(): Single<Wallet>

  fun observeDefaultWallet(): Observable<Wallet>

  fun savePassword(address: String, password: String): Completable
}