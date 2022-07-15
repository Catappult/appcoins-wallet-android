package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.Status
import com.appcoins.wallet.gamification.repository.entity.*
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.backup.BackupNotificationUtils
import com.asfoundation.wallet.backup.NotificationNeeded
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeLocalDataSource
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.AutoUpdateService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.transactions.PerkBonusAndGamificationService
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Named

class _IabLogicNavigator(private val iabLogic: _IabLogic, private val context: Context) :
  _Navigator {
  override fun navigate(navigation: _Navigation) {
    when (navigation) {
      is _Finish -> if (navigation.bundle?.getInt(AppcoinsBillingBinder.RESPONSE_CODE) == AppcoinsBillingBinder.RESULT_OK) {
        iabLogic.onBackupNotifications(navigation.bundle)
        iabLogic.onPerkNotifications(navigation.bundle)
      } else {
        navigate(_FinishActivity(navigation.bundle))
      }
      is _PerkBonusAndGamification -> PerkBonusAndGamificationService.buildService(
        context,
        navigation.address
      )
      is _BackupNotification -> BackupNotificationUtils.showBackupNotification(
        context,
        navigation.address
      )
    }
  }
}

@Suppress("LABEL_NAME_CLASH")
class _IabLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val billingAnalytics: BillingAnalytics,
  private var prefs: SharedPreferences,
  private val supportRepository: SupportRepository,
  @Named("local_version_code")
  private val walletVersionCode: Int,
  @Named("device-sdk")
  private val deviceSdk: Int,
  private val autoUpdateRepository: AutoUpdateService,
  private val walletInfoRepository: WalletInfoRepository,
  private val userStatsLocalData: UserStatsLocalData,
  private val gamificationApi: GamificationApi,
  private val promoCodeLocalDataSource: PromoCodeLocalDataSource,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private val transaction: TransactionBuilder?,
  private val pref: SharedPreferences,
) {

  private var firstImpression = false;

  private var autoUpdateModel = AutoUpdateModel()

  fun invoke() {
    handlePurchaseStartAnalytics()
    view.setState(_PaymentMethodsViewState)
  }

  fun onErrorDismiss() = navigator.navigate(_Close(Bundle()))

  fun onSupportClick() {
    showSupport()
  }

  fun onPerkNotifications(bundle: Bundle?) {
    getWalletAddress()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        navigator.navigate(_PerkBonusAndGamification(it))
        navigator.navigate(_FinishActivity(bundle))
      }
      .doOnError { navigator.navigate(_FinishActivity(bundle)) }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun onBackupNotifications(bundle: Bundle?) {
    incrementAndValidateNotificationNeeded()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { notificationNeeded ->
        if (notificationNeeded.isNeeded) {
          navigator.navigate(_BackupNotification(notificationNeeded.walletAddress))
        }
        navigator.navigate(_FinishActivity(bundle))
      }
      .doOnError { navigator.navigate(_Finish(bundle)) }
      .subscribe({ }, { it.printStackTrace() })
      .isDisposed
  }

  private fun handlePurchaseStartAnalytics() {
    Completable.fromAction {
      if (firstImpression) {
        if (hasPreSelectedPaymentMethod()) {
          billingAnalytics.sendPurchaseStartEvent(
            transaction?.domain,
            transaction?.skuId,
            transaction?.amount().toString(),
            getPreSelectedPaymentMethod(),
            transaction?.type,
            BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD
          )
        } else {
          billingAnalytics.sendPurchaseStartWithoutDetailsEvent(
            transaction?.domain,
            transaction?.skuId, transaction?.amount()
              .toString(), transaction?.type,
            BillingAnalytics.RAKAM_PAYMENT_METHOD
          )
        }
        firstImpression = false
      }
    }
      .subscribeOn(Schedulers.io())
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun onAutoUpdate() {
    getAutoUpdateModel()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .filter {
        hasRequiredHardUpdate(it.blackList, it.updateVersionCode, it.updateMinSdk)
      }
      .doOnSuccess { view.setState(_UpdateRequiredViewState) }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun onUserRegistration() {
    registerUser()
      .subscribeOn(Schedulers.io())
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun savePreselectedPaymentMethod(bundle: Bundle) {
    bundle.getString(PRE_SELECTED_PAYMENT_METHOD_KEY)
      ?.let { savePreSelectedPaymentMethod(it) }
  }

  private fun sendPayPalConfirmationEvent(action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transaction?.domain, transaction?.skuId,
      transaction?.amount()
        .toString(), "paypal",
      transaction?.type, action
    )
  }

  private fun sendCarrierBillingConfirmationEvent(action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transaction?.domain, transaction?.skuId,
      transaction?.amount()
        .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
      transaction?.type, action
    )
  }

  private fun sendPaypalUrlEvent(data: Intent) {
    val amountString = transaction?.amount().toString()
    billingAnalytics.sendPaypalUrlEvent(
      transaction?.domain, transaction?.skuId,
      amountString, "PAYPAL", getQueryParameter(data, "type"),
      getQueryParameter(data, "resultCode"), data.dataString
    )
  }

  private fun getQueryParameter(data: Intent, parameter: String): String? {
    return Uri.parse(data.dataString).getQueryParameter(parameter)
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      IabActivity.WEB_VIEW_REQUEST_CODE -> onWebViewResult(resultCode, data)
      IabActivity.AUTHENTICATION_REQUEST_CODE -> onAuthenticationResult(resultCode)
    }
  }

  private fun onWebViewResult(resultCode: Int, data: Intent?) {
    if (resultCode == WebViewActivity.FAIL) {
      if (data?.dataString?.contains("codapayments") != true) {
        if (data?.dataString?.contains(
            BillingWebViewFragment.CARRIER_BILLING_ONE_BIP_SCHEMA
          ) == true
        ) {
          sendCarrierBillingConfirmationEvent("cancel")
        } else {
          sendPayPalConfirmationEvent("cancel")
        }
      }
      if (data?.dataString?.contains(BillingWebViewFragment.OPEN_SUPPORT) == true) {
        showSupport()
      }
      view.setState(_PaymentMethodsViewState)
    } else if (resultCode == WebViewActivity.SUCCESS) {
      if (data?.scheme?.contains("adyencheckout") == true) {
        sendPaypalUrlEvent(data)
        if (getQueryParameter(data, "resultCode") == "cancelled")
          sendPayPalConfirmationEvent("cancel")
        else
          sendPayPalConfirmationEvent("buy")
      }
      view.setState(_WebViewResult(data!!.data))
    }
  }

  private fun onAuthenticationResult(resultCode: Int) {
    if (resultCode == AuthenticationPromptActivity.RESULT_OK) {
      view.setState(_AuthenticationSuccessViewState)
    } else if (resultCode == AuthenticationPromptActivity.RESULT_CANCELED) {
      view.setState(_AuthenticationFailViewState)
    }
  }

  /**
   * Flatten logic
   */

  private fun hasPreSelectedPaymentMethod(): Boolean {
    return prefs.contains(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY)
  }

  private fun getPreSelectedPaymentMethod(): String? {
    return prefs.getString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    )
  }

  private fun getWalletAddress(): Single<String> {
    return find().map(Wallet::address)
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
    pref.getString(_Erc681Logic.CURRENT_ACCOUNT_ADDRESS_KEY, null)

  private fun setCurrentWalletAddress(address: String) = pref.edit()
    .putString(CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun incrementAndValidateNotificationNeeded(): Single<NotificationNeeded> {
    return getWalletInfo(null, true, false)
      .flatMap { walletInfo: WalletInfo ->
        updateWalletPurchasesCount(walletInfo)
          .andThen(
            shouldShowSystemNotification(walletInfo)
              .flatMap { needed: Boolean? ->
                Single.just(
                  NotificationNeeded(needed!!, walletInfo.wallet)
                )
              }
          )
      }
  }

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

  private fun updateWalletPurchasesCount(walletInfo: WalletInfo): Completable {
    return if (walletInfo.hasBackup.not()) {
      Single.just(getWalletPurchasesCount(walletInfo.wallet))
        .map { it + 1 }
        .flatMapCompletable {
          incrementWalletPurchasesCount(walletInfo.wallet, it)
        }
    } else {
      Completable.complete()
    }
  }

  private fun getWalletPurchasesCount(walletAddress: String) =
    pref.getInt(WALLET_PURCHASES_COUNT + walletAddress, 0)

  private fun incrementWalletPurchasesCount(walletAddress: String, count: Int) =
    Completable.fromAction {
      pref.edit()
        .putInt(WALLET_PURCHASES_COUNT + walletAddress, count)
        .apply()
    }

  private fun shouldShowSystemNotification(walletInfo: WalletInfo): Single<Boolean> = Single.just(
    walletInfo.hasBackup.not()
        && meetsLastDismissCondition(walletInfo.wallet)
        && meetsCountConditions(walletInfo.wallet)
  )

  private fun meetsLastDismissCondition(walletAddress: String): Boolean {
    val savedTime = getDismissedBackupSystemNotificationSeenTime(walletAddress)
    val currentTime = System.currentTimeMillis()
    return currentTime >= savedTime + TimeUnit.DAYS.toMillis(DISMISS_PERIOD)
  }

  private fun meetsCountConditions(walletAddress: String): Boolean {
    val count = getWalletPurchasesCount(walletAddress)
    return count > 0 && count % PURCHASE_NOTIFICATION_THRESHOLD == 0
  }

  private fun getDismissedBackupSystemNotificationSeenTime(walletAddress: String) =
    pref.getLong(BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME + walletAddress, -1)

  private fun getAutoUpdateModel(invalidateCache: Boolean = true): Single<AutoUpdateModel> {
    return loadAutoUpdateModel(invalidateCache)
  }

  private fun loadAutoUpdateModel(invalidateCache: Boolean): Single<AutoUpdateModel> {
    if (autoUpdateModel.isValid() && !invalidateCache) {
      return Single.just(autoUpdateModel)
    }
    return autoUpdateRepository.loadAutoUpdateModel()
      .doOnSuccess { if (it.isValid()) autoUpdateModel = it }
  }

  private fun hasRequiredHardUpdate(
    blackList: List<Int>,
    updateVersionCode: Int,
    updateMinSdk: Int
  ): Boolean {
    return blackList.contains(walletVersionCode) && hasSoftUpdate(updateVersionCode, updateMinSdk)
  }

  private fun hasSoftUpdate(updateVersionCode: Int, updatedMinSdk: Int): Boolean {
    return walletVersionCode < updateVersionCode && deviceSdk >= updatedMinSdk
  }

  private fun registerUser() =
    getWalletAddress()
      .flatMap { address ->
        getCurrentPromoCode()
          .flatMap { promoCode ->
            getUserLevel(address, promoCode.code)
              .doOnSuccess { registerUser(it, address) }
          }
      }

  private fun getCurrentPromoCode(): Single<PromoCode> {
    return observeCurrentPromoCode().firstOrError()
  }

  private fun observeCurrentPromoCode(): Observable<PromoCode> =
    promoCodeLocalDataSource.observeSavedPromoCode()

  private fun getUserLevel(wallet: String, promoCodeString: String?): Single<Int> {
    return getGamificationLevel(wallet, promoCodeString)
  }

  private fun getGamificationLevel(wallet: String, promoCodeString: String?): Single<Int> {
    return getUserStats(wallet, promoCodeString)
      .filter { it.error == null }
      .map { mapToGamificationStats(it).level }
      .lastOrError()
      .onErrorReturn { GamificationStats.INVALID_LEVEL }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStats(
    wallet: String,
    promoCodeString: String?,
    offlineFirst: Boolean = true
  ): Observable<UserStats> {
    return getUserStatsFromResponses(wallet, promoCodeString, offlineFirst)
      .flatMap { userStatusResponse ->
        val gamification =
          userStatusResponse.promotions.firstOrNull { it is GamificationResponse } as GamificationResponse?
        if (userStatusResponse.error == null && !userStatusResponse.fromCache) {
          userStatsLocalData.setGamificationLevel(
            gamification?.level ?: GamificationStats.INVALID_LEVEL
          )
        }
        Observable.just(userStatusResponse)
      }
      .doOnError { it.printStackTrace() }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStatsFromResponses(
    wallet: String, promoCodeString: String?,
    offlineFirst: Boolean = true
  ): Observable<UserStats> {
    return if (offlineFirst) {
      Observable.concat(getUserStatsFromDB(wallet), getUserStatsFromAPI(wallet, promoCodeString))
    } else {
      getUserStatsFromAPI(wallet, promoCodeString, true)
    }
  }

  // NOTE: the use of the throwable parameter can be dropped once all usages in these repository
  //  follow offline first logic.
  private fun getUserStatsFromDB(
    wallet: String,
    throwable: Throwable? = null
  ): Observable<UserStats> {
    return Single.zip(
      userStatsLocalData.getPromotions(),
      userStatsLocalData.retrieveWalletOrigin(wallet)
    ) { promotions: List<PromotionsResponse>, walletOrigin: WalletOrigin ->
      Pair(promotions, walletOrigin)
    }
      .toObservable()
      .map { (promotions, walletOrigin) ->
        if (throwable == null) UserStats(promotions, walletOrigin, null, true)
        else mapErrorToUserStatsModel(promotions, walletOrigin, throwable)
      }
      .onErrorReturn {
        mapErrorToUserStatsModel(throwable ?: it, throwable == null)
      }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStatsFromAPI(
    wallet: String, promoCodeString: String?,
    useDbOnError: Boolean = false
  ): Observable<UserStats> {
    return gamificationApi.getUserStats(wallet, Locale.getDefault().language, promoCodeString)
      .map { filterByDate(it) }
      .flatMapObservable {
        userStatsLocalData.deleteAndInsertPromotions(it.promotions)
          .andThen(userStatsLocalData.insertWalletOrigin(wallet, it.walletOrigin))
          .toSingle { UserStats(it.promotions, it.walletOrigin) }
          .toObservable()
      }
      .onErrorResumeNext { throwable: Throwable ->
        if (useDbOnError) getUserStatsFromDB(wallet, throwable)
        else Observable.just(mapErrorToUserStatsModel(throwable, false))
      }
  }

  private fun mapToGamificationStats(stats: UserStats): GamificationStats {
    return if (stats.error != null) {
      map(stats.error!!, stats.fromCache)
    } else {
      val gamification =
        stats.promotions.firstOrNull { it is GamificationResponse } as GamificationResponse?
      if (gamification == null) {
        GamificationStats(GamificationStats.Status.UNKNOWN_ERROR, fromCache = stats.fromCache)
      } else {
        GamificationStats(
          GamificationStats.Status.OK, gamification.level,
          gamification.nextLevelAmount, gamification.bonus, gamification.totalSpend,
          gamification.totalEarned, PromotionsResponse.Status.ACTIVE == gamification.status,
          stats.fromCache
        )
      }
    }
  }

  private fun map(status: Status, fromCache: Boolean = false): GamificationStats {
    return if (status == Status.NO_NETWORK) {
      GamificationStats(GamificationStats.Status.NO_NETWORK, fromCache = fromCache)
    } else {
      GamificationStats(GamificationStats.Status.UNKNOWN_ERROR, fromCache = fromCache)
    }
  }

  private fun filterByDate(userStatusResponse: UserStatusResponse): UserStatusResponse {
    val validPromotions = userStatusResponse.promotions.filter { hasValidDate(it) }
    return UserStatusResponse(validPromotions, userStatusResponse.walletOrigin)
  }

  private fun hasValidDate(promotionsResponse: PromotionsResponse): Boolean {
    return if (promotionsResponse is GenericResponse) {
      val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      currentTime < promotionsResponse.endDate
    } else true
  }

  private fun mapErrorToUserStatsModel(
    promotions: List<PromotionsResponse>,
    walletOrigin: WalletOrigin,
    throwable: Throwable
  ): UserStats {
    return when {
      promotions.isEmpty() && isNoNetworkException(throwable) -> {
        throwable.printStackTrace()
        UserStats(Status.NO_NETWORK)
      }
      promotions.isEmpty() -> {
        throwable.printStackTrace()
        UserStats(Status.UNKNOWN_ERROR)
      }
      else -> UserStats(promotions, walletOrigin)
    }
  }

  private fun mapErrorToUserStatsModel(throwable: Throwable, fromCache: Boolean): UserStats {
    return if (isNoNetworkException(throwable)) {
      UserStats(Status.NO_NETWORK, fromCache)
    } else {
      UserStats(Status.UNKNOWN_ERROR, fromCache)
    }
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException ||
        throwable.cause != null && throwable.cause is IOException ||
        throwable is UnknownHostException
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

  private fun savePreSelectedPaymentMethod(paymentMethod: String?) {
    val editor = prefs.edit()
    editor.putString(PRE_SELECTED_PAYMENT_METHOD_KEY, paymentMethod)
    editor.putString(LAST_USED_PAYMENT_METHOD_KEY, paymentMethod)
    editor.apply()
  }

  private fun showSupport() = displayChatScreen()

  private fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client().displayMessenger()
  }

  companion object {
    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val WALLET_PURCHASES_COUNT = "wallet_purchases_count_"
    private const val BACKUP_SYSTEM_NOTIFICATION_SEEN_TIME = "backup_system_notification_seen_time_"
    private const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
    private const val LAST_USED_PAYMENT_METHOD_KEY = "LAST_USED_PAYMENT_METHOD_KEY"
    private const val PURCHASE_NOTIFICATION_THRESHOLD = 2
    private const val DISMISS_PERIOD = 30L
  }
}