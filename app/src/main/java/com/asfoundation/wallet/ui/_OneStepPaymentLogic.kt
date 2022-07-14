package com.asfoundation.wallet.ui

import android.content.SharedPreferences
import android.net.Uri
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.WalletGetterStatus
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.util.isOneStepURLString
import com.asfoundation.wallet.util.parseOneStep
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.schedulers.Schedulers
import org.kethereum.erc681.isEthereumURLString
import org.kethereum.erc681.parseERC681
import java.util.*
import kotlin.reflect.KFunction2


/**
 * https://${legacyPaymentHost}/transaction/...
 * https://${paymentHost}/transaction/...
 */

class _OneStepPaymentLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val eipTransactionParser: _EIPTransactionParser,
  private val oneStepTransactionParser: _OneStepTransactionParser,
  private val bdsApiSecondary: BdsApiSecondary,
  private val pref: SharedPreferences,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private val walletInfoRepository: WalletInfoRepository,
  private val passwordStore: PasswordStore,
  analytics: PaymentMethodsAnalytics,
  private val logger: Logger
) {

  init {
    analytics.startTimingForSdkTotalEvent()
  }

  fun invoke(data: String, callerPackage: String?, productName: String) {
    if (data.lowercase(Locale.ROOT).contains("/transaction/eskills")) {
      navigator.navigate(_StartESkills(data, callerPackage, productName))
    } else {
      handleWalletCreationIfNeeded()
        .takeUntil { it != WalletGetterStatus.CREATING.toString() }
        .filter { it != WalletGetterStatus.CREATING.toString() }
        .flatMap {
          parse(data)
            .flatMap { transaction: TransactionBuilder ->
              isWalletFromBds(transaction.domain, transaction.toAddress())
                .doOnSuccess { navigator.navigate(_StartTransfer(transaction, it)) }
            }
            .toObservable()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ }, {
          logger.log("OneStepPaymentReceiver", it)
          view.setState(_ErrorViewState())
        })
        .isDisposed
    }
  }

  private fun handleWalletCreationIfNeeded(): Observable<String> {
    return findWalletOrCreate()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        if (it == WalletGetterStatus.CREATING.toString()) view.setState(_CreatingWalletViewState)
      }
      .filter { it != WalletGetterStatus.CREATING.toString() }
      .map {
        view.setState(_WalletCreatedViewState)
        it
      }
  }

  private fun parse(data: String): Single<TransactionBuilder> = if (data.isEthereumURLString()) {
    Single.just(parseERC681(data))
      .map { erc681 -> eipTransactionParser.buildTransaction(erc681) }
  } else {
    if (Uri.parse(data).isOneStepURLString()) {
      Single.just(parseOneStep(Uri.parse(data)))
        .flatMap { oneStepUri -> oneStepTransactionParser.buildTransaction(oneStepUri, data) }
    } else {
      Single.error(RuntimeException("is not an supported URI"))
    }
  }

  private fun createWallet(password: String): Single<Wallet> =
    accountKeystoreService.createAccount(password)

  private fun deleteWallet(address: String, password: String): Completable =
    accountKeystoreService.deleteAccount(address, password)

  private fun exportWallet(
    address: String,
    password: String,
    newPassword: String?
  ): Single<String> =
    accountKeystoreService.exportAccount(address, password, newPassword)

  private fun findWalletOrCreate(): Observable<String> = find()
    .toObservable()
    .subscribeOn(Schedulers.single())
    .map { wallet -> wallet.address }
    .onErrorResumeNext { _: Throwable ->
      Observable.just(WalletGetterStatus.CREATING.toString())
        .mergeWith(create("Main Wallet").map { it.address }.toObservable())
    }

  private fun find(): Single<Wallet> = getDefaultWallet()
    .onErrorResumeNext {
      fetchWallets()
        .filter { it.isNotEmpty() }
        .map { it[0] }
        .flatMapCompletable { setDefaultWallet(it.address) }
        .andThen(getDefaultWallet())
    }

  private fun getDefaultWallet(): Single<Wallet> =
    Single.fromCallable { getDefaultWalletAddress() }
      .flatMap { address -> findWallet(address) }

  private fun getDefaultWalletAddress(): String {
    val currentWalletAddress = getCurrentWalletAddress()
    return currentWalletAddress ?: throw WalletNotFoundException()
  }

  private fun findWallet(address: String): Single<Wallet> {
    return fetchWallets().flatMap { accounts ->
      for (wallet in accounts) {
        if (wallet.hasSameAddress(address)) {
          return@flatMap Single.just(wallet)
        }
      }
      null
    }
  }

  private fun fetchWallets(): Single<Array<Wallet>> = accountKeystoreService.fetchAccounts()

  private fun setDefaultWallet(address: String) = Completable.fromAction {
    analyticsSetUp.setUserId(address)
    setCurrentWalletAddress(address)
  }

  private fun getCurrentWalletAddress() = pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)

  private fun setCurrentWalletAddress(address: String) = pref.edit()
    .putString(SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun isWalletFromBds(packageName: String?, wallet: String): Single<Boolean> {
    return if (packageName == null) {
      Single.just(false)
    } else getWallet(packageName)
      .map { anotherString: String? -> wallet.equals(anotherString, ignoreCase = true) }
      .onErrorReturn { throwable: Throwable -> false }
  }

  private fun create(name: String? = null): Single<Wallet> = passwordStore.generatePassword()
    .flatMap { passwordStore.setBackUpPassword(it).toSingleDefault(it) }
    .flatMap {
      createWallet(it)
        .compose(savePassword(passwordStore, this::deleteWallet, it))
        .flatMap { wallet: Wallet -> passwordVerification(wallet, it) }
        .flatMap { wallet: Wallet ->
          walletInfoRepository.updateWalletName(wallet.address, name)
            .toSingleDefault(wallet)
        }
    }

  private fun passwordVerification(wallet: Wallet, masterPassword: String): Single<Wallet> =
    passwordStore.getPassword(wallet.address)
      .flatMap { exportWallet(wallet.address, it, it) }
      .flatMap { findWallet(wallet.address) }
      .onErrorResumeNext { throwable: Throwable ->
        deleteWallet(wallet.address, masterPassword)
          .lift(completableErrorProxy(throwable))
          .toSingle { wallet }
      }


  private fun getWallet(packageName: String): Single<String?> {
    return bdsApiSecondary.getWallet(packageName).map { it.data.address }
  }

  private fun savePassword(
    passwordStore: PasswordStore,
    deleteWallet: KFunction2<String, String, Completable>,
    password: String
  ): SingleTransformer<Wallet, Wallet> {
    return SavePasswordOperator(passwordStore, deleteWallet, password)
  }

  private fun completableErrorProxy(throwable: Throwable): CompletableOperator {
    return CompletableErrorProxyOperator(throwable)
  }

  private class SavePasswordOperator(
    private val passwordStore: PasswordStore,
    private val deleteWallet: KFunction2<String, String, Completable>,
    private val password: String
  ) : SingleTransformer<Wallet, Wallet> {
    override fun apply(upstream: Single<Wallet>): Single<Wallet> {
      return upstream.flatMap { wallet: Wallet ->
        passwordStore.setPassword(wallet.address, password)
          .onErrorResumeNext { err: Throwable ->
            deleteWallet(wallet.address, password)
              .lift(CompletableErrorProxyOperator(err))
          }
          .toSingle { wallet }
      }
    }
  }

  private class CompletableErrorProxyOperator(throwable: Throwable) :
    CompletableOperator {
    private val throwable: Throwable
    override fun apply(observer: CompletableObserver): CompletableObserver {
      return object : DisposableCompletableObserver() {
        override fun onComplete() {
          if (!isDisposed) {
            observer.onError(throwable)
          }
        }

        override fun onError(ex: Throwable) {
          if (!isDisposed) {
            observer.onError(ex)
          }
        }
      }
    }

    init {
      this.throwable = throwable
    }
  }

  companion object {
    const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
  }
}