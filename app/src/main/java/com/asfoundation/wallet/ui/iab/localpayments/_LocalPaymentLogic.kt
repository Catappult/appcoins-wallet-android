package com.asfoundation.wallet.ui.iab.localpayments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import com.appcoins.wallet.appcoins.rewards.ErrorInfo.ErrorType
import com.appcoins.wallet.appcoins.rewards.ErrorMapper
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import com.asfoundation.wallet.entity.AppcToFiatResponseBody
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.SignDataStandardNormalizer
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.WalletUtils
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.web3j.crypto.Keys
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class _LocalPaymentLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val data: LocalPaymentData,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private var pref: SharedPreferences,
  private val passwordStore: PasswordStore,
  private val promoCodeRepository: PromoCodeRepository,
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val brokerBdsApi: RemoteRepository.BrokerBdsApi,
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationRepository.BrokerVerificationApi,
  private val subsApi: SubscriptionBillingApi,
  private val inappBdsApi: RemoteRepository.InappBdsApi,
  private val tokenToFiatApi: TokenRateService.TokenToFiatApi,
  private val supportRepository: SupportRepository,
  private val analytics: LocalPaymentAnalytics,
  private val context: Context?,
  private val logger: Logger,
  private val errorMapper: ErrorMapper
) {

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: android.util.Pair<String, ECKey>? = null

  private var waitingResult: Boolean = false

  fun present(savedInstance: Bundle?) {
    view.setState(_BonusUiViewState(data.bonus))
    savedInstance?.let {
      waitingResult = savedInstance.getBoolean(WAITING_RESULT)
    }
    onViewCreatedRequestLink()
  }

  fun onStop() {
    waitingResult = false
  }

  fun preparePendingUserPayment() {
    Single.zip(
      getPaymentMethodIcon(),
      getApplicationIcon()
    ) { paymentMethodIcon: Bitmap, applicationIcon: Bitmap ->
      Pair(paymentMethodIcon, applicationIcon)
    }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ view.setState(_PendingUserPaymentViewState(data.label, it.first, it.second)) },
        { showError(it) })
      .isDisposed
  }

  private fun getPaymentMethodIcon() = Single.fromCallable {
    GlideApp.with(context!!)
      .asBitmap()
      .load(data.paymentMethodIconUrl)
      .override(getWidth(), getHeight())
      .centerCrop()
      .submit()
      .get()
  }

  private fun getApplicationIcon() = Single.fromCallable {
    val applicationIcon =
      (context!!.packageManager.getApplicationIcon(data.packageName) as BitmapDrawable).bitmap

    Bitmap.createScaledBitmap(applicationIcon, appIconWidth, appIconHeight, true)
  }

  private fun onViewCreatedRequestLink() {
    getPaymentLink(
      packageName = data.packageName,
      fiatAmount = data.fiatAmount,
      fiatCurrency = data.currency,
      paymentMethod = data.paymentId,
      productName = data.skuId,
      type = data.type,
      origin = data.origin,
      walletDeveloper = data.developerAddress,
      developerPayload = data.payload,
      callbackUrl = data.callbackUrl,
      orderReference = data.orderReference,
      referrerUrl = data.referrerUrl
    )
      .filter { !waitingResult }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        analytics.sendNavigationToUrlEvents(
          packageName = data.packageName,
          skuId = data.skuId,
          amount = data.appcAmount.toString(),
          type = data.type,
          paymentId = data.paymentId
        )
        navigator.navigate(_GoToUriForResult(it))
        waitingResult = true
      }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({ }, { showError(it) })
      .isDisposed
  }

  fun onPaymentRedirect(uri: Uri) {
    Observable.just(uri)
      .doOnNext { view.setState(_ProcessingLoadingViewState) }
//      .doOnNext { view.lockRotation() }
      .flatMap {
        getTransaction(it, data.async)
          .subscribeOn(Schedulers.io())
      }
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable { handleTransactionStatus(it) }
      .subscribe({}, { showError(it) })
      .isDisposed
  }

  fun onOkErrorButtonClick() {
    navigator.navigate(_FinishWithError)
  }

  fun onOkBuyButtonClick() {
    navigator.navigate(_Close(Bundle()))
  }

  private fun handleFraudFlow() {
    isWalletBlocked()
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .flatMap { blocked ->
        if (blocked) {
          isWalletVerified()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess {
              if (it) view.setState(_ErrorViewState(R.string.purchase_error_wallet_block_code_403))
              else view.setState(_VerificationViewState())
            }
        } else {
          Single.just(true)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { view.setState(_ErrorViewState(R.string.purchase_error_wallet_block_code_403)) }
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorViewState(R.string.purchase_error_wallet_block_code_403))
      })
      .isDisposed
  }

  private fun handleTransactionStatus(transaction: Transaction): Completable {
    view.setState(_LoadedViewState)
    return when {
      isErrorStatus(transaction) -> Completable.fromAction {
        logger.log(TAG, "Transaction came with error status: ${transaction.status}")
        view.setState(_ErrorViewState())
      }
        .subscribeOn(AndroidSchedulers.mainThread())
      data.async ->
        //Although this should no longer happen at the moment in Iab, since it doesn't consume much process time
        //I decided to leave this here in case the API wants to change the logic and return them to Iab in the future.
        handleAsyncTransactionStatus(transaction)
          .andThen(Completable.fromAction {
            savePreSelectedPaymentMethod(data.paymentId)
            saveAsyncLocalPayment(data.paymentId)
            preparePendingUserPayment()
          })
      transaction.status == Status.COMPLETED -> handleSyncCompletedStatus(transaction)
      else -> Completable.complete()
    }
  }

  private fun isErrorStatus(transaction: Transaction) =
    transaction.status == Status.FAILED ||
        transaction.status == Status.CANCELED ||
        transaction.status == Status.INVALID_TRANSACTION

  private fun handleSyncCompletedStatus(transaction: Transaction): Completable {
    return getCompletePurchaseBundle(
      type = data.type,
      merchantName = data.packageName,
      sku = data.skuId,
      purchaseUid = transaction.metadata?.purchaseUid,
      orderReference = transaction.orderReference,
      hash = transaction.hash,
      scheduler = Schedulers.io()
    )
      .doOnSuccess {
        analytics.sendPaymentConclusionEvents(
          packageName = data.packageName,
          skuId = data.skuId,
          amount = data.appcAmount,
          type = data.type,
          paymentId = data.paymentId
        )
        handleRevenueEvent()
      }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable {
        Completable.fromAction { view.setState(_CompletedPaymentViewState) }
          .andThen(
            Completable.timer(
              -1 /* complete_payment_view.lottie_transaction_success.duration */,
              TimeUnit.MILLISECONDS
            )
          )
          .andThen(Completable.fromAction {
            it.bundle.putString(
              InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
              data.paymentId
            )
            navigator.navigate(_Finish(it.bundle))
          })
      }
  }

  private fun handleRevenueEvent() {
    convertToFiat(
      data.appcAmount.toDouble(),
      BillingAnalytics.EVENT_REVENUE_CURRENCY
    )
      .subscribeOn(Schedulers.io())
      .doOnSuccess { fiatValue -> analytics.sendRevenueEvent(fiatValue.amount.toString()) }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun handleAsyncTransactionStatus(transaction: Transaction): Completable {
    return when (transaction.status) {
      Status.PENDING_USER_PAYMENT -> {
        Completable.fromAction {
          analytics.sendPendingPaymentEvents(
            packageName = data.packageName,
            skuId = data.skuId,
            amount = data.appcAmount.toString(),
            type = data.type,
            paymentId = data.paymentId
          )
        }
      }
      Status.COMPLETED -> {
        Completable.fromAction {
          analytics.sendPaymentConclusionEvents(
            packageName = data.packageName,
            skuId = data.skuId,
            amount = data.appcAmount,
            type = data.type,
            paymentId = data.paymentId
          )
          handleRevenueEvent()
        }
      }
      else -> Completable.complete()
    }
  }

  fun onSupportClicks() {
    showSupport(data.gamificationLevel)
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun showError(throwable: Throwable) {
    logger.log(TAG, throwable)
    val error = errorMapper.map(throwable)
    when (error.errorType) {
      ErrorType.SUB_ALREADY_OWNED -> view.setState(_ErrorViewState(R.string.subscriptions_error_already_subscribed))
      ErrorType.BLOCKED -> handleFraudFlow()
      else -> view.setState(_ErrorViewState(R.string.unknown_error))
    }
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  private fun getWidth(): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 184f,
      context?.resources?.displayMetrics
    )
      .toInt()
  }

  private fun getHeight(): Int {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 80f,
      context?.resources?.displayMetrics
    )
      .toInt()
  }

  private val appIconWidth: Int
    get() = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 160f,
      context?.resources?.displayMetrics
    )
      .toInt()

  private val appIconHeight: Int
    get() = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_PX, 160f,
      context?.resources?.displayMetrics
    )
      .toInt()

  /**
   * Flatten logic
   */

  private fun getPaymentLink(
    packageName: String,
    fiatAmount: String?,
    fiatCurrency: String?,
    paymentMethod: String,
    productName: String?,
    type: String,
    origin: String?,
    walletDeveloper: String?,
    developerPayload: String?,
    callbackUrl: String?,
    orderReference: String?,
    referrerUrl: String?
  ): Single<String> {
    return getAndSignCurrentWalletAddress()
      .flatMap { walletAddressModel ->
        getAttributionEntity(packageName)
          .flatMap { attributionEntity ->
            getCurrentPromoCode().flatMap { promoCode ->
              createLocalPaymentTransaction(
                paymentId = paymentMethod,
                packageName = packageName,
                price = fiatAmount,
                currency = fiatCurrency,
                productName = productName,
                type = type,
                origin = origin,
                walletsDeveloper = walletDeveloper,
                entityOemId = attributionEntity.oemId,
                entityDomain = attributionEntity.domain,
                entityPromoCode = promoCode.code,
                developerPayload = developerPayload,
                callback = callbackUrl,
                orderReference = orderReference,
                referrerUrl = referrerUrl,
                walletAddress = walletAddressModel.address,
                walletSignature = walletAddressModel.signedAddress
              )
            }
          }
          .map { it.url }
      }
  }

  private fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKey(wallet)
        .map { sign(normalizer.normalize(Keys.toChecksumAddress(wallet.address)), it) }
        .map { WalletAddressModel(wallet.address, it) }
    }

  private fun find(): Single<Wallet> {
    return getDefaultWallet()
      .subscribeOn(Schedulers.io())
      .onErrorResumeNext {
        fetchWallets()
          .filter { wallets -> wallets.isNotEmpty() }
          .map { wallets: Array<Wallet> -> wallets[0] }
          .flatMapCompletable { wallet: Wallet -> setDefaultWallet(wallet.address) }
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
    .putString(SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun getPrivateKey(wallet: Wallet): Single<ECKey> =
    if (stringECKeyPair?.first?.equals(wallet.address, true) == true) {
      Single.just(stringECKeyPair!!.second)
    } else {
      passwordStore.getPassword(wallet.address)
        .flatMap { password ->
          accountKeystoreService.exportAccount(wallet.address, password, password)
            .map { json ->
              ECKey.fromPrivate(WalletUtils.loadCredentials(password, json).ecKeyPair.privateKey)
            }
        }
        .doOnSuccess { ecKey -> stringECKeyPair = android.util.Pair(wallet.address, ecKey) }
    }

  private fun getCurrentPromoCode(): Single<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
      .firstOrError()
  }

  private fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
  }

  @Throws(Exception::class)
  private fun sign(plainText: String, ecKey: ECKey): String =
    ecKey.sign(HashUtil.sha3(plainText.toByteArray())).toHex()

  private fun createLocalPaymentTransaction(
    paymentId: String,
    packageName: String,
    price: String?,
    currency: String?,
    productName: String?,
    type: String,
    origin: String?,
    walletsDeveloper: String?,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    developerPayload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    walletAddress: String,
    walletSignature: String
  ): Single<Transaction> =
    brokerBdsApi.createTransaction(
      origin = origin,
      domain = packageName,
      priceValue = price,
      priceCurrency = currency,
      product = productName,
      type = type,
      userWallet = walletAddress,
      walletsDeveloper = walletsDeveloper,
      entityOemId = entityOemId,
      entityDomain = entityDomain,
      entityPromoCode = entityPromoCode,
      method = paymentId,
      developerPayload = developerPayload,
      callback = callback,
      orderReference = orderReference,
      referrerUrl = referrerUrl,
      walletAddress = walletAddress,
      walletSignature = walletSignature
    )

  private fun getTransaction(uri: Uri, async: Boolean): Observable<Transaction> =
    getTransaction(uri.lastPathSegment!!)
      .filter { isEndingState(it.status, async) }
      .distinctUntilChanged { transaction -> transaction.status }

  private fun getTransaction(uid: String): Observable<Transaction> {
    return Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.io())
      .timeInterval()
      .switchMap { longTimed -> getAppcoinsTransaction(uid, Schedulers.io()).toObservable() }
  }

  private fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap { getAppcoinsTransaction(uid, it.address, it.signedAddress) }
  }

  private fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> =
    brokerBdsApi.getAppcoinsTransaction(uid, address, signedContent)

  private fun isEndingState(status: Status, async: Boolean) =
    (status == Status.PENDING_USER_PAYMENT && async) ||
        status == Status.COMPLETED ||
        status == Status.FAILED ||
        status == Status.CANCELED ||
        status == Status.INVALID_TRANSACTION

  private fun isWalletBlocked(): Single<Boolean> {
    return getWalletInfo(null, cached = false, updateFiat = false)
      .map { walletInfo -> walletInfo.blocked }
      .onErrorReturn { false }
      .delay(1, TimeUnit.SECONDS)
  }

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

  private fun isWalletVerified() =
    getAndSignCurrentWalletAddress()
      .flatMap { isVerified(it.address, it.signedAddress) }
      .onErrorReturn { true }

  private fun isVerified(address: String, signature: String): Single<Boolean> {
    return getVerificationStatus(address, signature)
      .map { status -> status == VerificationStatus.VERIFIED }
  }

  private fun getVerificationStatus(
    walletAddress: String,
    walletSignature: String
  ): Single<VerificationStatus> {
    return walletInfoRepository.getLatestWalletInfo(walletAddress, updateFiatValues = false)
      .subscribeOn(Schedulers.io())
      .flatMap { walletInfo ->
        if (walletInfo.verified) {
          return@flatMap Single.just(VerificationStatus.VERIFIED)
        } else {
          if (getCachedValidationStatus(walletAddress) == VerificationStatus.VERIFYING) {
            return@flatMap Single.just(VerificationStatus.VERIFYING)
          }
          return@flatMap getCardVerificationState(walletAddress, walletSignature)
        }
      }
      .doOnSuccess { status -> saveVerificationStatus(walletAddress, status) }
      .onErrorReturn {
        if (it.isNoNetworkException()) VerificationStatus.NO_NETWORK
        else VerificationStatus.ERROR
      }
  }

  private fun getCardVerificationState(
    walletAddress: String,
    walletSignature: String
  ): Single<VerificationStatus> {
    return brokerVerificationApi.getVerificationState(walletAddress, walletSignature)
      .map { verificationState ->
        if (verificationState == "ACTIVE") VerificationStatus.CODE_REQUESTED
        else VerificationStatus.UNVERIFIED
      }
      .onErrorReturn {
        if (it.isNoNetworkException()) VerificationStatus.NO_NETWORK
        else VerificationStatus.ERROR
      }
  }

  private fun saveVerificationStatus(walletAddress: String, status: VerificationStatus) {
    pref.edit()
      .putInt(WALLET_VERIFIED + walletAddress, status.ordinal)
      .apply()
  }

  private fun getCachedValidationStatus(walletAddress: String) =
    VerificationStatus.values()[pref.getInt(WALLET_VERIFIED + walletAddress, 4)]

  private fun savePreSelectedPaymentMethod(paymentMethod: String) {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.putString(PRE_SELECTED_PAYMENT_METHOD_KEY, paymentMethod)
    editor.putString(LAST_USED_PAYMENT_METHOD_KEY, paymentMethod)
    editor.apply()
  }

  private fun saveAsyncLocalPayment(paymentMethod: String) {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.putString(LOCAL_PAYMENT_METHOD_KEY, paymentMethod)
    editor.apply()
  }

  private fun getCompletePurchaseBundle(
    type: String,
    merchantName: String,
    sku: String?,
    purchaseUid: String?,
    orderReference: String?,
    hash: String?,
    scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    return getCompletedPurchaseBundle(
      type = type,
      merchantName = merchantName,
      sku = sku,
      purchaseUid = purchaseUid,
      orderReference = orderReference,
      hash = hash,
      scheduler = scheduler
    )
  }

  private fun getCompletedPurchaseBundle(
    type: String,
    merchantName: String,
    sku: String?,
    purchaseUid: String?,
    orderReference: String?,
    hash: String?,
    scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    val billingType = BillingSupportedType.valueOfInsensitive(type)
    return if (isManagedTransaction(billingType) && sku != null) {
      getSkuPurchase(merchantName, sku, purchaseUid, scheduler, billingType)
        .map { purchase: Purchase ->
          PurchaseBundleModel(mapPurchase(purchase, orderReference), purchase.renewal)
        }
    } else {
      Single.just(PurchaseBundleModel(successBundle(hash), null))
    }
  }

  private fun isManagedTransaction(type: BillingSupportedType): Boolean =
    type === BillingSupportedType.INAPP || type === BillingSupportedType.INAPP_SUBSCRIPTION

  private fun getSkuPurchase(
    merchantName: String,
    sku: String?,
    purchaseUid: String?,
    scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<Purchase> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        getSkuPurchase(merchantName, sku, purchaseUid, it.address, it.signedAddress, type)
      }
  }

  private fun mapPurchase(purchase: Purchase, orderReference: String?): Bundle {
    return mapPurchase(
      purchaseId = purchase.uid,
      signature = purchase.signature.value,
      signatureData = purchase.signature.message,
      orderReference = orderReference
    )
  }

  private fun mapPurchase(
    purchaseId: String,
    signature: String,
    signatureData: String,
    orderReference: String?
  ): Bundle {
    val intent = Bundle()
    intent.putString(AppcoinsBillingBinder.INAPP_PURCHASE_ID, purchaseId)
    intent.putString(AppcoinsBillingBinder.INAPP_PURCHASE_DATA, signatureData)
    intent.putString(AppcoinsBillingBinder.INAPP_DATA_SIGNATURE, signature)
    intent.putString(AppcoinsBillingBinder.INAPP_ORDER_REFERENCE, orderReference)
    intent.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    return intent
  }

  private fun successBundle(uid: String?): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    bundle.putString(TRANSACTION_HASH, uid)
    return bundle
  }

  private fun getSkuPurchase(
    packageName: String,
    skuId: String?,
    purchaseUid: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Purchase> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      getSkuPurchase(packageName, skuId, walletAddress, walletSignature)
    } else {
      getSkuPurchaseSubs(packageName, purchaseUid!!, walletAddress, walletSignature)
    }

  private fun getSkuPurchase(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    inappBdsApi.getPurchases(
      packageName = packageName,
      walletAddress = walletAddress,
      walletSignature = walletSignature,
      type = BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT),
      sku = skuId
    )
      .map {
        if (it.items.isEmpty()) {
          throw HttpException(
            Response.error<GetPurchasesResponse>(
              404, "{}".toResponseBody("application/json".toMediaType())
            )
          )
        }
        mapGetPurchasesResponse(packageName, it)[0]
      }

  private fun getSkuPurchaseSubs(
    packageName: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    subsApi.getPurchase(packageName, purchaseUid, walletAddress, walletSignature)
      .map { mapSubscriptionPurchaseResponse(packageName, it) }

  private fun mapGetPurchasesResponse(
    packageName: String,
    purchasesResponse: GetPurchasesResponse
  ): List<Purchase> {
    return purchasesResponse.items.map { mapInappPurchaseResponse(packageName, it) }
  }

  private fun mapInappPurchaseResponse(
    packageName: String,
    inAppPurchaseResponse: InappPurchaseResponse
  ): Purchase = Purchase(
    uid = inAppPurchaseResponse.uid,
    product = RemoteProduct(inAppPurchaseResponse.sku),
    state = mapPurchaseState(inAppPurchaseResponse.state),
    autoRenewing = false,
    renewal = null,
    packageName = Package(packageName),
    signature = Signature(
      value = inAppPurchaseResponse.verification.signature,
      message = inAppPurchaseResponse.verification.data
    )
  )

  private fun mapPurchaseState(state: PurchaseState): State {
    return when (state) {
      PurchaseState.CONSUMED -> State.CONSUMED
      PurchaseState.PENDING -> State.PENDING
      PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }

  private fun mapSubscriptionPurchaseResponse(
    packageName: String,
    subscriptionPurchaseResponse: SubscriptionPurchaseResponse
  ): Purchase {
    return Purchase(
      uid = subscriptionPurchaseResponse.uid,
      product = RemoteProduct(subscriptionPurchaseResponse.sku),
      state = mapPurchaseState(subscriptionPurchaseResponse.state),
      autoRenewing = subscriptionPurchaseResponse.autoRenewing,
      renewal = mapRenewalDate(subscriptionPurchaseResponse.renewal),
      packageName = Package(packageName),
      signature = Signature(
        value = subscriptionPurchaseResponse.verification.signature,
        message = subscriptionPurchaseResponse.verification.data
      )
    )
  }

  private fun mapPurchaseState(state: com.appcoins.wallet.bdsbilling.PurchaseState): State {
    return when (state) {
      com.appcoins.wallet.bdsbilling.PurchaseState.CONSUMED -> State.CONSUMED
      com.appcoins.wallet.bdsbilling.PurchaseState.PENDING -> State.PENDING
      com.appcoins.wallet.bdsbilling.PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }

  private fun mapRenewalDate(renewal: String?): Date? {
    return if (renewal == null) null
    else {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
      dateFormat.parse(renewal)
    }
  }

  private fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> =
    getTokenValue(currency)
      .map { fiatValueConversion: FiatValue ->
        calculateValue(fiatValueConversion, appcValue)
      }

  private fun calculateValue(fiatValue: FiatValue, appcValue: Double): FiatValue =
    FiatValue(
      fiatValue.amount.multiply(BigDecimal.valueOf(appcValue)),
      fiatValue.currency,
      fiatValue.symbol
    )

  private fun getTokenValue(currency: String): Single<FiatValue> = getAppcRate(currency)

  private fun getAppcRate(currency: String): Single<FiatValue> {
    return tokenToFiatApi.getAppcToFiatRate(currency)
      .map { appcToFiatResponseBody -> appcToFiatResponseBody }
      .map(AppcToFiatResponseBody::appcValue)
      .map { FiatValue(it, currency, "") }
      .subscribeOn(Schedulers.io())
      .singleOrError()
  }

  private fun showSupport(gamificationLevel: Int): Completable {
    return getWalletAddress()
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable { showSupport(it, gamificationLevel) }
      .subscribeOn(Schedulers.io())
  }

  private fun getWalletAddress(): Single<String> = find()
    .map { Keys.toChecksumAddress(it.address) }

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

  companion object {
    private val TAG = _LocalPaymentLogic::class.java.simpleName
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val TRANSACTION_HASH = "transaction_hash"

    private const val WALLET_VERIFIED = "wallet_verified_cc_"

    private const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
    private const val LOCAL_PAYMENT_METHOD_KEY = "LOCAL_PAYMENT_METHOD_KEY"
    private const val LAST_USED_PAYMENT_METHOD_KEY = "LAST_USED_PAYMENT_METHOD_KEY"
  }
}

