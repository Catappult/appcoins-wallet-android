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
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.util.isOneStepURLString
import com.asfoundation.wallet.util.parseOneStep
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import java.util.*


/**
 * https://${legacyPaymentHost}/transaction/...
 * https://${paymentHost}/transaction/...
 */

class _OneStepPaymentLogic(
  private val view: _View,
  private val navigator: _Navigator,
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
      try {
        handleWalletCreationIfNeeded()
        val transaction = parse(data)
        val isBds = transaction.domain
          ?.run {
            isWalletFromBds(
              transaction.domain,
              transaction.toAddress()
            )
          } ?: false
        navigator.navigate(_StartTransfer(transaction, isBds))
      } catch (it: Throwable) {
        logger.log("OneStepPaymentReceiver", it)
        view.setState(_ErrorViewState())
      }
    }
  }

  private fun handleWalletCreationIfNeeded(): String {
    return try {
      find()
    } catch (e: Throwable) {
      view.setState(_CreatingWalletViewState)
      create("Main Wallet")
    }
      .address
      .also {
        view.setState(_WalletCreatedViewState)
      }
  }

  private fun parse(data: String): TransactionBuilder =
    Uri.parse(data)
      .takeIf { it.isOneStepURLString() }
      ?.let { parseOneStep(it) }
      ?.let { oneStepTransactionParser.buildTransaction(it, data) }
      ?: throw RuntimeException("is not an supported URI")

  private fun createWallet(password: String): Wallet =
    accountKeystoreService.createAccount(password).blockingGet()

  private fun deleteWallet(address: String, password: String) =
    accountKeystoreService.deleteAccount(address, password).blockingAwait()

  private fun exportWallet(
    address: String,
    password: String,
    newPassword: String?
  ): String =
    accountKeystoreService.exportAccount(address, password, newPassword).blockingGet()

  private fun find(): Wallet = try {
    getDefaultWallet()
  } catch (e: Throwable) {
    fetchWallets()[0]
      .let {
        setDefaultWallet(it.address)
        getDefaultWallet()
      }
  }

  private fun getDefaultWallet(): Wallet =
    findWallet(getDefaultWalletAddress())

  private fun getDefaultWalletAddress(): String =
    getCurrentWalletAddress() ?: throw WalletNotFoundException()

  private fun findWallet(address: String): Wallet = fetchWallets()
    .let { accounts ->
      for (wallet in accounts) {
        if (wallet.hasSameAddress(address)) {
          return wallet
        }
      }
      throw WalletNotFoundException()
    }

  private fun fetchWallets(): Array<Wallet> = accountKeystoreService.fetchAccounts().blockingGet()

  private fun setDefaultWallet(address: String) {
    analyticsSetUp.setUserId(address)
    setCurrentWalletAddress(address)
  }

  private fun getCurrentWalletAddress() = pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)

  private fun setCurrentWalletAddress(address: String) = pref.edit()
    .putString(SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun isWalletFromBds(packageName: String, wallet: String): Boolean =
    try {
      getWallet(packageName)
        .let { wallet.equals(it, ignoreCase = true) }
    } catch (e: Throwable) {
      false
    }

  private fun create(name: String): Wallet = passwordStore.generatePassword()
    .blockingGet()
    .let {
      passwordStore.setBackUpPassword(it).blockingAwait()
      createWallet(it)
        .let { wallet: Wallet ->
          try {
            passwordStore.setPassword(wallet.address, it).blockingAwait()
            wallet
          } catch (throwable: Throwable) {
            deleteWallet(wallet.address, it)
            throw throwable
          }
        }
        .let { wallet -> passwordVerification(wallet, it) }
        .let { wallet ->
          walletInfoRepository.updateWalletName(wallet.address, name).blockingAwait()
          wallet
        }
    }

  private fun passwordVerification(wallet: Wallet, masterPassword: String): Wallet = try {
    passwordStore.getPassword(wallet.address).blockingGet()
      .let { exportWallet(wallet.address, it, it) }
      .let { findWallet(wallet.address) }
  } catch (throwable: Throwable) {
    deleteWallet(wallet.address, masterPassword)
    throw throwable
  }

  private fun getWallet(packageName: String): String =
    bdsApiSecondary.getWallet(packageName).blockingGet().data.address

  companion object {
    const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
  }
}