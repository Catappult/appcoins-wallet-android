package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import com.asfoundation.wallet.analytics.AmplitudeAnalytics
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.rx.operator.Operators
import com.asfoundation.wallet.recover.FailedRecover
import com.asfoundation.wallet.recover.RecoverWalletResult
import com.asfoundation.wallet.recover.SuccessfulRecover
import com.asfoundation.wallet.recover.use_cases.ExtractWalletAddressUseCase
import com.asfoundation.wallet.recover.use_cases.RecoverErrorMapperUseCase
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.reactivex.*

class WalletRepository(private val preferencesRepositoryType: PreferencesRepositoryType,
                       private val accountKeystoreService: AccountKeystoreService,
                       private val networkScheduler: Scheduler,
                       private val analyticsSetUp: AnalyticsSetup,
                       private val amplitudeAnalytics: AmplitudeAnalytics,
                       private val walletInfoRepository: WalletInfoRepository,
                       private val recoverErrorMapperUseCase: RecoverErrorMapperUseCase,
                       private val extractWalletAddressUseCase: ExtractWalletAddressUseCase,
                       private val passwordStore: PasswordStore,
                       private val currencyFormatUtils: CurrencyFormatUtils) :
    WalletRepositoryType {

  override fun fetchWallets(): Single<Array<Wallet>> {
    return accountKeystoreService.fetchAccounts()
  }

  override fun findWallet(address: String): Single<Wallet> {
    return fetchWallets().flatMap { accounts ->
      for (wallet in accounts) {
        if (wallet.sameAddress(address)) {
          return@flatMap Single.just(wallet)
        }
      }
      null
    }
  }

  override fun createWallet(password: String): Single<Wallet> {
    return accountKeystoreService.createAccount(password)
  }

  override fun restoreKeystoreToWallet(store: String, password: String,
                                       newPassword: String): Single<RecoverWalletResult> {
    return extractWalletAddressUseCase(keystore = store)
        .flatMap { walletAddress ->
          walletInfoRepository.getLatestWalletInfo(walletAddress, updateFiatValues = true)
              .flatMap { walletInfo ->
                accountKeystoreService.restoreKeystore(store, password, newPassword)
                    .compose(Operators.savePassword(passwordStore, this, newPassword))
                    .map {
                      return@map SuccessfulRecover(walletInfo.wallet) as RecoverWalletResult
                    }
                    .onErrorReturn {
                      recoverErrorMapperUseCase(keystore = store, throwable = it,
                          address = walletInfo.wallet,
                          amount = currencyFormatUtils.formatCurrency(
                              walletInfo.walletBalance.overallFiat.amount),
                          symbol = walletInfo.walletBalance.overallFiat.symbol)
                    }
              }

        }
  }

//  fun restoreKeystoreToWallet1(store: String, password: String,
//                               newPassword: String): Single<RecoverWalletResult> {
//    return accountKeystoreService.restoreKeystore(store, password, newPassword)
//        .compose(Operators.savePassword(passwordStore, this, newPassword))
//        .flatMap { wallet ->
//          walletInfoRepository.getLatestWalletInfo(wallet.address, updateFiatValues = true)
//              .map { walletInfo ->
//                return@map SuccessfulRecover(walletInfo.wallet) as RecoverWalletResult
//              }
//              .onErrorReturn {
//                recoverErrorMapperUseCase(keystore = store, throwable = it,
//                    address = walletInfo.wallet,
//                    amount = currencyFormatUtils.formatCurrency(
//                        walletInfo.walletBalance.overallFiat.amount),
//                    symbol = walletInfo.walletBalance.overallFiat.symbol)
//              }
//        }
//  }

  override fun restorePrivateKeyToWallet(privateKey: String?,
                                         newPassword: String): Single<RecoverWalletResult> {
    return accountKeystoreService.restorePrivateKey(privateKey, newPassword)
        .compose(Operators.savePassword(passwordStore, this, newPassword))
        .map { wallet ->
          return@map SuccessfulRecover(wallet.address) as RecoverWalletResult
        }
        .onErrorReturn {
          FailedRecover.InvalidPrivateKey
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
      amplitudeAnalytics.setUserId(address)
      preferencesRepositoryType.setCurrentWalletAddress(address)
    }
  }

  override fun getDefaultWallet(): Single<Wallet> {
    return Single.fromCallable { getDefaultWalletAddress() }
        .flatMap { address -> findWallet(address) }
  }

  private fun getDefaultWalletAddress(): String {
    val currentWalletAddress = preferencesRepositoryType.getCurrentWalletAddress()
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
                if (key == SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY) {
                  emitWalletAddress(emitter)
                }
              }
          emitter.setCancellable { preferencesRepositoryType.removeChangeListener(listener) }
          emitWalletAddress(emitter)
          preferencesRepositoryType.addChangeListener(listener)
        } as ObservableOnSubscribe<String>)
        .flatMapSingle { address -> findWallet(address) }
  }

  fun getCurrentWallet(): Single<Wallet> {
    return getDefaultWallet()
        .onErrorResumeNext {
          fetchWallets()
              .filter { wallets -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                setDefaultWallet(wallet.address)
              }
              .andThen(getDefaultWallet())
        }
  }

}