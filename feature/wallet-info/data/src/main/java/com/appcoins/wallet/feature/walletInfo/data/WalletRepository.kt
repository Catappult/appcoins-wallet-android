package com.appcoins.wallet.feature.walletInfo.data

import android.content.SharedPreferences
import com.appcoins.wallet.core.analytics.analytics.AnalyticsSetup
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.authentication.rxOperator.Operators
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletNotFoundException
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource.Companion.CURRENT_ACCOUNT_ADDRESS_KEY
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
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
              SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
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