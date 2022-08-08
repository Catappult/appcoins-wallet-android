package com.asfoundation.wallet.ui.iab

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Pair
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TokenInfo
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.APPC
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.CREDITS
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.web3j.crypto.Keys
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class _MergedAppcoinsLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val analytics: BillingAnalytics,
  private val formatter: CurrencyFormatUtils,
  private val pref: SharedPreferences,
  private val walletInfoRepository: WalletInfoRepository,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private val defaultTokenRepository: _DefaultTokenRepository,
  private val supportRepository: SupportRepository,
  private val gamificationLevel: Int,
  private val logger: Logger,
  private val transactionBuilder: TransactionBuilder,
  private val isSubscription: Boolean
) {

  private var cachedSelectedPaymentId: String? = null

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let { cachedSelectedPaymentId = it.getString(SELECTED_PAYMENT_ID) }
    if (isSubscription) view.setState(_VolatilityInfoState)
  }

  fun onResume() {
    fetchBalance()
  }

  private fun fetchBalance() {
    getWalletInfo(null, cached = true, updateFiat = false)
      .map { walletInfo ->
        val appcFiatBalance = walletInfo.walletBalance.appcBalance.fiat
        val ethFiatBalance = walletInfo.walletBalance.ethBalance.fiat
        val creditsFiatBalance = walletInfo.walletBalance.creditsBalance.fiat
        val creditsAmount = walletInfo.walletBalance.creditsBalance.token.amount
        val appcFiatValue =
          FiatValue(
            appcFiatBalance.amount.plus(ethFiatBalance.amount),
            appcFiatBalance.currency,
            appcFiatBalance.symbol
          )
        MergedAppcoinsBalance(appcFiatValue, creditsFiatBalance, creditsAmount)
      }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { balance ->
        val appcFiat =
          formatter.formatPaymentCurrency(balance.appcFiatValue.amount, WalletCurrency.APPCOINS)
        val creditsFiat = formatter.formatPaymentCurrency(
          balance.creditsFiatBalance.amount,
          WalletCurrency.CREDITS
        )
        view.setState(
          _BalanceValuesViewState(
            appcFiat,
            creditsFiat,
            balance.creditsFiatBalance.currency
          )
        )
      }
      .observeOn(Schedulers.io())
      .flatMap {
        // This should be refactored to avoid repeated calls to WalletInfo
        Single.zip(
          hasEnoughCredits(it.creditsAppcAmount),
          retrieveAppcAvailability(transactionBuilder, isSubscription)
        ) { hasCredits: Availability, hasAppc: Availability ->
          Pair(hasCredits, hasAppc)
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        view.setState(
          _PaymentsInformationViewState(
            it.first.isAvailable,
            it.first.disableReason,
            it.second.isAvailable,
            it.second.disableReason
          )
        )
//        view.toggleSkeletons(false)
      }
//      .doOnSubscribe { view.toggleSkeletons(true) }
      .subscribe({ }, { it.printStackTrace() })
      .isDisposed
  }

  private fun hasEnoughCredits(creditsAppcAmount: BigDecimal): Single<Availability> {
    return Single.fromCallable {
      val available = creditsAppcAmount >= transactionBuilder.amount()
      val disabledReason =
        if (!available) R.string.purchase_appcoins_credits_noavailable_body else null
      Availability(available, disabledReason)
    }
  }

  fun onBackClick(paymentInfoWrapper: PaymentInfoWrapper) {
    Observable.just(paymentInfoWrapper)
      .observeOn(Schedulers.io())
      .doOnNext { paymentMethod ->
        analytics.sendPaymentConfirmationEvent(
          paymentMethod.packageName,
          paymentMethod.skuDetails, paymentMethod.value, paymentMethod.purchaseDetails,
          paymentMethod.transactionType, "cancel"
        )
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { view.setState(_PaymentMethodsViewState) }
      .subscribe({}, { showError(it) })
      .isDisposed
  }

  fun onAuthenticationResult(auth: Boolean) {
    Observable.just(auth)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        if (!it || cachedSelectedPaymentId == null) {
          view.setState(_LoadedViewState)
        } else {
          navigateToPayment(cachedSelectedPaymentId!!)
        }
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun navigateToPayment(selectedPaymentId: String) {
    when (mapPaymentMethods(selectedPaymentId)) {
      PaymentMethodsView.SelectedPaymentMethod.APPC -> view.setState(
        _AppcPaymentViewState(transactionBuilder)
      )
      PaymentMethodsView.SelectedPaymentMethod.APPC_CREDITS -> view.setState(
        _CreditsPaymentViewState(transactionBuilder)
      )
      else -> {
        view.setState(_ErrorViewState(R.string.unknown_error))
        logger.log(TAG, "Wrong payment method after authentication.")
      }
    }
  }

  fun onBuyClick(paymentInfoWrapper: PaymentInfoWrapper) {
    Observable.just(paymentInfoWrapper)
      .observeOn(Schedulers.io())
      .doOnNext { paymentMethod ->
        analytics.sendPaymentConfirmationEvent(
          paymentMethod.packageName,
          paymentMethod.skuDetails, paymentMethod.value, paymentMethod.purchaseDetails,
          paymentMethod.transactionType, "buy"
        )
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { view.setState(_LoadedViewState) }
      .flatMapSingle { paymentMethod ->
        isWalletBlocked()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSuccess {
            if (hasAuthenticationPermission()) {
              cachedSelectedPaymentId = map(paymentMethod.purchaseDetails)
              view.setState(_AuthenticationViewState)
            } else {
              handleBuyClickSelection(paymentMethod.purchaseDetails)
            }
          }
      }
      .subscribe({}, {
        view.setState(_LoadedViewState)
        showError(it)
      })
      .isDisposed
  }

  private fun map(purchaseDetails: String): String {
    if (purchaseDetails == APPC) return PaymentMethodsView.PaymentMethodId.APPC.id
    else if (purchaseDetails == CREDITS) return PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    return ""
  }

  fun onSupportClicks() {
    showSupport(gamificationLevel)
  }

  fun onErrorDismiss() {
    navigator.navigate(_Close(Bundle()))
  }

  fun onPaymentSelectionChange(selection: String) {
    Observable.just(selection)
      .doOnNext { handleSelection(it) }
      .subscribe({}, { showError(it) })
      .isDisposed
  }

  private fun showError(t: Throwable) {
    logger.log(TAG, t)
    if (t.isNoNetworkException()) {
      view.setState(_ErrorViewState(R.string.notification_no_network_poa))
    } else {
      view.setState(_ErrorViewState(R.string.activity_iab_error_message))
    }
  }

  private fun handleBuyClickSelection(selection: String) {
    when (selection) {
      APPC -> view.setState(
        _AppcPaymentViewState(transactionBuilder)
      )
      CREDITS -> view.setState(
        _CreditsPaymentViewState(transactionBuilder)
      )
      else -> Log.w(TAG, "No appcoins payment method selected")
    }
  }

  private fun handleSelection(selection: String) {
    when (selection) {
      APPC -> {
        view.setState(_HideVolatilityInfoState)
        view.setState(
          _BonusViewState(
            R.string.subscriptions_bonus_body.takeIf { isSubscription }
              ?: R.string.gamification_purchase_body
          )
        )
      }
      CREDITS -> {
        view.setState(_HideBonusViewState)
        if (isSubscription) {
          view.setState(_VolatilityInfoState)
        }
      }
      else -> Log.w(TAG, "Error creating PublishSubject")
    }
  }

  fun onSavedInstanceState(outState: Bundle) {
    outState.putString(SELECTED_PAYMENT_ID, cachedSelectedPaymentId)
  }

  /**
   * Flatten logic
   */

  /**
   * Retrieves WalletInfo
   *
   * @param address Wallet address, or null to use the currently active wallet
   * @param cached true to return the cached WalletInfo, or false if it should retrieve from network
   * @param updateFiat true if it should also update fiat, or false if not necessary
   */
  private fun getWalletInfo(
    address: String?,
    cached: Boolean,
    updateFiat: Boolean
  ): Single<WalletInfo> {
    val walletAddressSingle =
      address?.let { Single.just(Wallet(address)) } ?: getCurrentWallet()
    return if (cached) {
      walletAddressSingle.flatMap {
        walletInfoRepository.getCachedWalletInfo(it.address)
      }
    } else {
      walletAddressSingle.flatMap {
        walletInfoRepository.getLatestWalletInfo(it.address, updateFiat)
      }
    }
  }

  private fun getCurrentWallet(): Single<Wallet> {
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
    pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)

  private fun setCurrentWalletAddress(address: String) = pref.edit()
    .putString(SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun isWalletBlocked(): Single<Boolean> {
    return getWalletInfo(null, cached = false, updateFiat = false)
      .map { walletInfo -> walletInfo.blocked }
      .onErrorReturn { false }
      .delay(1, TimeUnit.SECONDS)
  }

  private fun retrieveAppcAvailability(
    transactionBuilder: TransactionBuilder,
    isSubscription: Boolean
  ): Single<Availability> {
    return if (isSubscription) {
      //TODO replace for correct string
      // Note that currently this is not available (only Adyen is available for subscriptions)
      // Since it is not available server-side, these developments don't really matter right now.
      // We should revisit this if there ever is support for subscriptions with APPC
      Single.just(Availability(false, R.string.subscriptions_details_disclaimer))
    } else {
      getBalanceState(transactionBuilder)
        .map {
          when (it) {
            BalanceState.NO_ETHER -> Availability(
              false,
              R.string.purchase_no_eth_body
            )
            BalanceState.NO_TOKEN, BalanceState.NO_ETHER_NO_TOKEN -> Availability(
              false, R.string.purchase_no_appcoins_body
            )
            BalanceState.OK -> Availability(true, null)
          }
        }
    }
  }

  private fun getBalanceState(transactionBuilder: TransactionBuilder): Single<BalanceState> {
    val transactionGasLimit = transactionBuilder.gasSettings().gasLimit
    val gasSettings = transactionBuilder.gasSettings()
    return if (transactionBuilder.shouldSendToken()) {
      checkTokenAddress(transactionBuilder, true).flatMap {
        Single.zip(
          hasEnoughForTransfer(transactionBuilder),
          hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit))
        ) { enoughEther, enoughTokens ->
          mapToState(enoughEther, enoughTokens)
        }
      }
    } else {
      Single.zip(
        hasEnoughForTransfer(transactionBuilder),
        hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit))
      ) { enoughEther, enoughTokens ->
        mapToState(enoughEther, enoughTokens)
      }
    }
  }

  private fun <T> checkTokenAddress(
    transactionBuilder: TransactionBuilder,
    successValue: T
  ): Single<T> {
    return getDefaultToken()
      .flatMap { tokenInfo: TokenInfo ->
        if (tokenInfo.address.equals(transactionBuilder.contractAddress(), ignoreCase = true)) {
          return@flatMap Single.just(successValue)
        } else {
          return@flatMap Single.error<T>(UnknownTokenException())
        }
      }
  }

  private fun getDefaultToken(): Single<TokenInfo> {
    return Single.fromCallable { defaultTokenRepository.tokenInfo }
  }

  private fun hasEnoughForTransfer(transactionBuilder: TransactionBuilder): Single<Boolean> {
    return if (transactionBuilder.shouldSendToken()) {
      hasEnoughBalance(
        address = null,
        value = transactionBuilder.amount(),
        unit = Convert.Unit.ETHER,
        balanceType = BalanceType.APPC
      )
    } else {
      hasEnoughBalance(
        address = null,
        value = transactionBuilder.amount(),
        unit = Convert.Unit.WEI,
        balanceType = BalanceType.ETH
      )
    }
  }

  private fun hasEnoughForFee(fee: BigDecimal): Single<Boolean> {
    return hasEnoughBalance(
      address = null,
      value = fee,
      unit = Convert.Unit.WEI,
      balanceType = BalanceType.ETH
    )
  }

  private fun hasEnoughBalance(
    address: String?,
    value: BigDecimal,
    unit: Convert.Unit,
    balanceType: BalanceType
  ): Single<Boolean> {
    return getWalletInfo(address, cached = false, updateFiat = false)
      .flatMap { walletInfo ->
        val scaledValue = Convert.toWei(value, unit)
        val scaledCredits = Convert.toWei(
          walletInfo.walletBalance.creditsBalance.token.amount,
          Convert.Unit.ETHER
        )
        val scaledAppc =
          Convert.toWei(walletInfo.walletBalance.appcBalance.token.amount, Convert.Unit.ETHER)
        val scaledEth =
          Convert.toWei(walletInfo.walletBalance.ethBalance.token.amount, Convert.Unit.ETHER)
        return@flatMap when (balanceType) {
          BalanceType.APPC_C -> Single.just(scaledCredits >= scaledValue)
          BalanceType.APPC -> Single.just(scaledAppc >= scaledValue)
          BalanceType.ETH -> Single.just(scaledEth >= scaledValue)
        }
      }
  }

  private fun mapToState(
    enoughEther: Boolean,
    enoughTokens: Boolean
  ): BalanceState {
    return if (enoughTokens && enoughEther) {
      BalanceState.OK
    } else if (!enoughTokens && !enoughEther) {
      BalanceState.NO_ETHER_NO_TOKEN
    } else if (enoughEther) {
      BalanceState.NO_TOKEN
    } else {
      BalanceState.NO_ETHER
    }
  }

  private fun hasAuthenticationPermission(): Boolean {
    return pref.getBoolean(AUTHENTICATION_PERMISSION, false)
  }

  private fun showSupport(gamificationLevel: Int): Completable = getWalletAddress()
    .observeOn(AndroidSchedulers.mainThread())
    .flatMapCompletable { showSupport(it, gamificationLevel) }
    .subscribeOn(Schedulers.io())

  private fun getWalletAddress(): Single<String> = find()
    .map { Keys.toChecksumAddress(it.address) }

  private fun find(): Single<Wallet> = getDefaultWallet()
    .onErrorResumeNext {
      fetchWallets()
        .filter { it.isNotEmpty() }
        .map { it[0] }
        .flatMapCompletable { setDefaultWallet(it.address) }
        .andThen(getDefaultWallet())
    }

  private fun showSupport(walletAddress: String, gamificationLevel: Int): Completable =
    Completable.fromAction {
      registerUser(gamificationLevel, walletAddress)
      displayChatScreen()
    }

  private fun registerUser(level: Int, walletAddress: String) {
    // force lowercase to make sure 2 users are not registered with the same wallet address, where
    // one has uppercase letters (to be check summed), and the other does not
    val address = walletAddress.lowercase(Locale.ROOT)
    val currentUser = supportRepository.getCurrentUser()
    if (currentUser.userAddress != address || currentUser.gamificationLevel != level) {
      if (currentUser.userAddress != address) {
        Intercom.client()
          .logout()
      }
      supportRepository.saveNewUser(address, level)
    }
  }

  private fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client().displayMessenger()
  }

  private fun mapPaymentMethods(paymentId: String): PaymentMethodsView.SelectedPaymentMethod {
    return when (paymentId) {
      "ask_friend" -> PaymentMethodsView.SelectedPaymentMethod.SHARE_LINK
      "paypal" -> PaymentMethodsView.SelectedPaymentMethod.PAYPAL
      "credit_card" -> PaymentMethodsView.SelectedPaymentMethod.CREDIT_CARD
      "appcoins" -> PaymentMethodsView.SelectedPaymentMethod.APPC
      "appcoins_credits" -> PaymentMethodsView.SelectedPaymentMethod.APPC_CREDITS
      "merged_appcoins" -> PaymentMethodsView.SelectedPaymentMethod.MERGED_APPC
      "earn_appcoins" -> PaymentMethodsView.SelectedPaymentMethod.EARN_APPC
      "onebip" -> PaymentMethodsView.SelectedPaymentMethod.CARRIER_BILLING
      "" -> PaymentMethodsView.SelectedPaymentMethod.ERROR
      else -> PaymentMethodsView.SelectedPaymentMethod.LOCAL_PAYMENTS
    }
  }

  private enum class BalanceState {
    NO_TOKEN, NO_ETHER, NO_ETHER_NO_TOKEN, OK
  }

  private enum class BalanceType {
    APPC, ETH, APPC_C
  }

  companion object {
    private val TAG = MergedAppcoinsFragment::class.java.simpleName
    private const val SELECTED_PAYMENT_ID = "selected_paymentId"
    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"

    private const val AUTHENTICATION_PERMISSION = "authentication_permission"
  }
}
