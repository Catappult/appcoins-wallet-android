package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.rx.operator.Operators
import com.asfoundation.wallet.recover.result.RestoreResult
import com.asfoundation.wallet.recover.result.SuccessfulRestore
import com.asfoundation.wallet.service.AccountKeystoreService
import io.reactivex.*
import it.czerwinski.android.hilt.annotations.BoundTo
import repository.CommonsPreferencesDataSource
import repository.CommonsPreferencesDataSource.Companion.CURRENT_ACCOUNT_ADDRESS_KEY
import javax.inject.Inject

@BoundTo(supertype = WalletRepositoryType::class)
class WalletRepository @Inject constructor(
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private val passwordStore: PasswordStore
) : WalletRepositoryType {

  override fun fetchWallets(): Single<Array<Wallet>> {
    return accountKeystoreService.fetchAccounts()
  }

  override fun findWallet(address: String): Single<Wallet> {
    return fetchWallets().map { accounts ->
      for (wallet in accounts) {
        if (wallet.hasSameAddress(address)) {
          return@map wallet
        }
      }
      throw NullPointerException("No wallets found")
    }
  }

  override fun createWallet(password: String): Single<Wallet> {
    return accountKeystoreService.createAccount(password)
  }

  override fun restoreKeystoreToWallet(store: String, password: String,
                                       newPassword: String): Single<RestoreResult> {
    return accountKeystoreService.restoreKeystore(store, password, newPassword)
        .flatMap { restoreResult ->
          var savePasswordCompletable = Completable.complete()
          if (restoreResult is SuccessfulRestore) {
            savePasswordCompletable = savePassword(restoreResult.address, newPassword)
          }
          return@flatMap savePasswordCompletable
              .andThen(Single.just(restoreResult))
        }
  }

  override fun restorePrivateKeyToWallet(privateKey: String?,
                                         newPassword: String): Single<RestoreResult> {
    return accountKeystoreService.restorePrivateKey(privateKey, newPassword)
        .flatMap { restoreResult ->
          var savePasswordCompletable = Completable.complete()
          if (restoreResult is SuccessfulRestore) {
            savePasswordCompletable = savePassword(restoreResult.address, newPassword)
          }
          return@flatMap savePasswordCompletable
              .andThen(Single.just(restoreResult))
        }
  }

  override fun exportWallet(address: String, password: String,
                            newPassword: String?): Single<String> {
    return accountKeystoreService.exportAccount(address, password, newPassword)
  }

  override fun deleteWallet(address: String, password: String): Completable {
    return accountKeystoreService.deleteAccount(address, password)
  }

  override fun setDefaultWallet(address: String): Completable {
    return Completable.fromAction {
      analyticsSetUp.setUserId(address)
      commonsPreferencesDataSource.setCurrentWalletAddress(address)
    }
  }

  override fun getDefaultWallet(): Single<Wallet> {
    return Single.fromCallable { getDefaultWalletAddress() }
        .flatMap { address -> findWallet(address) }
  }

  private fun getDefaultWalletAddress(): String {
    val currentWalletAddress = commonsPreferencesDataSource.getCurrentWalletAddress()
    return currentWalletAddress ?: throw WalletNotFoundException()
  }

  private fun emitWalletAddress(emitter: ObservableEmitter<String>) {
    try {
      val walletAddress = getDefaultWalletAddress()
      emitter.onNext(walletAddress)
    } catch (e: WalletNotFoundException) {
      emitter.tryOnError(e)
    }
  }

  override fun observeDefaultWallet(): Observable<Wallet> {
    return Observable.create(
        ObservableOnSubscribe { emitter: ObservableEmitter<String> ->
          val listener =
              SharedPreferences.OnSharedPreferenceChangeListener { _, key: String ->
                if (key == CURRENT_ACCOUNT_ADDRESS_KEY) {
                  emitWalletAddress(emitter)
                }
              }
          emitter.setCancellable { commonsPreferencesDataSource.removeChangeListener(listener) }
          emitWalletAddress(emitter)
          commonsPreferencesDataSource.addChangeListener(listener)
        } as ObservableOnSubscribe<String>)
        .flatMapSingle { address -> findWallet(address) }
  }

  override fun savePassword(address: String, password: String): Completable {
    return passwordStore.setPassword(address, password)
          .onErrorResumeNext { throwable ->
            deleteWallet(address, password)
                .lift(Operators.completableErrorProxy(throwable))
          }
  }
}