package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.recover.RecoverWalletResult
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface WalletRepositoryType {

  fun fetchWallets(): Single<Array<Wallet>>

  fun findWallet(address: String): Single<Wallet>

  fun createWallet(password: String): Single<Wallet>

  fun restoreKeystoreToWallet(store: String, password: String,
                              newPassword: String): Single<RecoverWalletResult>

  fun restorePrivateKeyToWallet(privateKey: String?, newPassword: String): Single<RecoverWalletResult>

  fun exportWallet(address: String, password: String, newPassword: String?): Single<String>

  fun deleteWallet(address: String, password: String): Completable

  fun setDefaultWallet(address: String): Completable

  fun getDefaultWallet(): Single<Wallet>

  fun observeDefaultWallet(): Observable<Wallet>
}