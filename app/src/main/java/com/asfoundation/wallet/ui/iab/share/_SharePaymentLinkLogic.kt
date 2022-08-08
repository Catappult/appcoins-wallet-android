package com.asfoundation.wallet.ui.iab.share

import android.content.SharedPreferences
import android.os.Bundle
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.share.BdsShareLinkRepository
import com.asfoundation.wallet.billing.share.ShareLinkData
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class _SharePaymentLinkLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val analytics: BillingAnalytics,
  private var pref: SharedPreferences,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private var api: BdsShareLinkRepository.BdsShareLinkApi
) {

  fun onShare(sharePaymentData: SharePaymentLinkFragmentView.SharePaymentData) {
    Observable.just(sharePaymentData)
      .doOnNext { view.setState(_FetchingLinkInfoViewState) }
      .flatMapSingle {
        analytics.sendPaymentConfirmationEvent(
          it.domain,
          it.skuId ?: "",
          it.amount,
          it.paymentMethod,
          it.type,
          "share"
        )
        getLink(it)
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        savePreSelectedPaymentMethod(PaymentMethodsView.PaymentMethodId.ASK_FRIEND.id)
        view.setState(_ShareLink2ViewState(it))
      }
      .subscribe({}, {
        it.printStackTrace()
        view.setState(_ErrorViewState())
      })
      .isDisposed
  }

  fun onStop(sharePaymentData: SharePaymentLinkFragmentView.SharePaymentData) {
    Observable.just(sharePaymentData)
      .doOnNext {
        analytics.sendPaymentConfirmationEvent(
          it.domain,
          it.skuId ?: "",
          it.amount,
          it.paymentMethod,
          it.type,
          "close"
        )
        navigator.navigate(_Close(Bundle()))
      }
      .subscribe()
      .isDisposed
  }

  private fun getLink(data: SharePaymentLinkFragmentView.SharePaymentData): Single<String> {
    return Single.zip(
      Single.timer(1, TimeUnit.SECONDS),
      getLinkToShare(
        domain = data.domain,
        skuId = data.skuId,
        message = data.message,
        originalAmount = data.originalAmount,
        originalCurrency = data.originalCurrency,
        paymentMethod = data.paymentMethod
      )
        .subscribeOn(Schedulers.io())
    ) { _: Long, url: String -> url }
  }

  /**
   * Flatten logic
   */

  private fun savePreSelectedPaymentMethod(paymentMethod: String?) {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.putString(PRE_SELECTED_PAYMENT_METHOD_KEY, paymentMethod)
    editor.putString(LAST_USED_PAYMENT_METHOD_KEY, paymentMethod)
    editor.apply()
  }

  private fun getLinkToShare(
    domain: String,
    skuId: String?,
    message: String?,
    originalAmount: String?,
    originalCurrency: String?,
    paymentMethod: String
  ): Single<String> {
    return find()
      .flatMap {
        getLink(
          domain = domain,
          skuId = skuId,
          message = message,
          walletAddress = it.address,
          originalAmount = originalAmount,
          originalCurrency = originalCurrency,
          paymentMethod = paymentMethod
        )
      }
  }

  private fun find(): Single<Wallet> {
    return getDefaultWallet()
      .subscribeOn(Schedulers.io())
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

  private fun getCurrentWalletAddress() =
    pref.getString(_OneStepPaymentLogic.CURRENT_ACCOUNT_ADDRESS_KEY, null)

  private fun setCurrentWalletAddress(address: String) = pref.edit()
    .putString(SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun getLink(
    domain: String,
    skuId: String?,
    message: String?,
    walletAddress: String,
    originalAmount: String?,
    originalCurrency: String?,
    paymentMethod: String
  ): Single<String> {
    return api.getPaymentLink(
      ShareLinkData(
        packageName = domain,
        sku = skuId,
        walletAddress = walletAddress,
        message = message,
        amount = originalAmount,
        currency = originalCurrency,
        method = paymentMethod
      )
    )
      .map { it.url }
  }

  companion object {
    private const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
    private const val LAST_USED_PAYMENT_METHOD_KEY = "LAST_USED_PAYMENT_METHOD_KEY"
  }
}
