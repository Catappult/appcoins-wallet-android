package com.asfoundation.wallet.ui.iab

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Pair
import com.appcoins.wallet.appcoins.rewards.Transaction
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.commons.Repository
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import com.asfoundation.wallet.entity.AppcToFiatResponseBody
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.SignDataStandardNormalizer
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.util.CurrencyFormatUtils
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

class _AppcoinsRewardsBuyLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val cache: Repository<String, Transaction>,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private var pref: SharedPreferences,
  private val passwordStore: PasswordStore,
  private val inappBdsApi: RemoteRepository.InappBdsApi,
  private val subsApi: SubscriptionBillingApi,
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationRepository.BrokerVerificationApi,
  private val supportRepository: SupportRepository,
  private val packageName: String,
  private val isBds: Boolean,
  private val isPreSelected: Boolean,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics,
  private val transactionBuilder: TransactionBuilder,
  private val formatter: CurrencyFormatUtils,
  private val gamificationLevel: Int,
  private val tokenToFiatApi: TokenRateService.TokenToFiatApi,
  private val logger: Logger
) {

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: Pair<String, ECKey>? = null

  fun present() {
    onBuyClick()
  }

  private fun onOkErrorClick() {
    view.setState(_ErrorViewState())
  }

  private fun onBuyClick() {
    pay(
      sku = transactionBuilder.skuId,
      amount = transactionBuilder.amount(),
      developerAddress = transactionBuilder.toAddress(),
      packageName = packageName,
      origin = getOrigin(isBds, transactionBuilder),
      type = transactionBuilder.type,
      payload = transactionBuilder.payload,
      callbackUrl = transactionBuilder.callbackUrl,
      orderReference = transactionBuilder.orderReference,
      referrerUrl = transactionBuilder.referrerUrl,
      productToken = transactionBuilder.productToken
    )
      .andThen(
        getPaymentStatus(
          packageName,
          transactionBuilder.skuId,
          transactionBuilder.amount()
        )
      )
      .subscribeOn(Schedulers.io())
      .flatMapCompletable {
        handlePaymentStatus(it, transactionBuilder.skuId, transactionBuilder.amount())
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe {
        paymentAnalytics.startTimingForPurchaseEvent()
        view.setState(_LoadingViewState)
      }
      .doOnError {
        logger.log(TAG, it)
        view.setState(_ErrorCodeViewState())
      }
      .subscribe({}, {})
      .isDisposed
  }

  private fun getOrigin(isBds: Boolean, transaction: TransactionBuilder): String? =
    if (transaction.origin == null) {
      if (isBds) "BDS" else null
    } else {
      transaction.origin
    }

  private fun handlePaymentStatus(
    transaction: RewardPayment,
    sku: String?,
    amount: BigDecimal
  ): Completable {
    sendPaymentErrorEvent(transaction)
    return when (transaction.status) {
      Status.PROCESSING -> Completable.fromAction { view.setState(_LoadingViewState) }
      Status.COMPLETED -> {
        if (isBds && isManagedPaymentType(transactionBuilder.type)) {
          val billingType = BillingSupportedType.valueOfProductType(transactionBuilder.type)
          getPaymentCompleted(packageName, sku, transaction.purchaseUid, billingType)
            .flatMapCompletable { purchase ->
              Completable.fromAction { view.setState(_TransactionCompletedViewState) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .andThen(
                  Completable.timer(
                    -1 /* lottie_transaction_success.getDuration() */,
                    TimeUnit.MILLISECONDS
                  )
                )
                .andThen(Completable.fromAction { removeAsyncLocalPayment() })
                .andThen(Completable.fromAction { finish(purchase, transaction.orderReference) })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorResumeNext {
              Completable.fromAction {
                logger.log(TAG, "Error after completing the transaction", it)
                view.setState(_ErrorCodeViewState())
              }
            }
        } else {
          getTransaction(packageName, sku, amount).firstOrError()
            .map(Transaction::txId).flatMapCompletable { transactionId ->
              Completable.fromAction { view.setState(_TransactionCompletedViewState) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .andThen(
                  Completable.timer(
                    -1 /* lottie_transaction_success.getDuration() */,
                    TimeUnit.MILLISECONDS
                  )
                )
                .andThen(Completable.fromAction { finish(transactionId) })
            }
        }
      }
      Status.ERROR -> Completable.fromAction {
        logger.log(TAG, "Credits error: ${transaction.errorMessage}")
        view.setState(_ErrorCodeViewState())
      }.subscribeOn(AndroidSchedulers.mainThread())
      Status.FORBIDDEN -> Completable.fromAction {
        logger.log(TAG, "Forbidden")
        handleFraudFlow()
      }
      Status.SUB_ALREADY_OWNED -> Completable.fromAction {
        logger.log(TAG, "Sub already owned")
        view.setState(_ErrorCodeViewState(R.string.subscriptions_error_already_subscribed))

      }.subscribeOn(AndroidSchedulers.mainThread())
      Status.NO_NETWORK -> Completable.fromAction {
        view.setState(_NoNetworkErrorViewState)
      }.subscribeOn(AndroidSchedulers.mainThread())
    }
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
              if (it) view.setState(_ErrorCodeViewState(R.string.purchase_error_wallet_block_code_403))
              else view.setState(_VerificationViewState())
            }
        } else {
          Single.just(true)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { view.setState(_ErrorCodeViewState(R.string.purchase_error_wallet_block_code_403)) }
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorCodeViewState(R.string.purchase_error_wallet_block_code_403))
      })
      .isDisposed
  }

  fun sendPaymentEvent() {
    analytics.sendPaymentEvent(
      packageName,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      BillingAnalytics.PAYMENT_METHOD_REWARDS,
      transactionBuilder.type
    )
  }

  fun sendRevenueEvent() {
    analytics.sendRevenueEvent(
      formatter.scaleFiat(
        convertToFiat(
          transactionBuilder.amount().toDouble(),
          BillingAnalytics.EVENT_REVENUE_CURRENCY
        ).blockingGet()
          .amount
      ).toString()
    )
  }

  fun sendPaymentSuccessEvent() {
    paymentAnalytics.stopTimingForPurchaseEvent(
      PaymentMethodsAnalytics.PAYMENT_METHOD_APPC,
      true,
      isPreSelected
    )
    analytics.sendPaymentSuccessEvent(
      packageName,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      BillingAnalytics.PAYMENT_METHOD_REWARDS,
      transactionBuilder.type
    )
  }

  private fun sendPaymentErrorEvent(transaction: RewardPayment) {
    val status = transaction.status
    if (isErrorStatus(status)) {
      paymentAnalytics.stopTimingForPurchaseEvent(
        PaymentMethodsAnalytics.PAYMENT_METHOD_APPC,
        true,
        isPreSelected
      )
      if (transaction.errorCode == null && transaction.errorMessage == null) {
        analytics.sendPaymentErrorEvent(
          packageName,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_REWARDS,
          transactionBuilder.type,
          status.toString()
        )
      } else {
        analytics.sendPaymentErrorWithDetailsEvent(
          packageName,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_REWARDS,
          transactionBuilder.type,
          transaction.errorCode.toString(),
          transaction.errorMessage.toString()
        )
      }
    }
  }

  private fun isErrorStatus(status: Status): Boolean =
    status === Status.ERROR || status === Status.NO_NETWORK || status === Status.FORBIDDEN || status === Status.SUB_ALREADY_OWNED

  fun onSupportClicks() {
    showSupport(gamificationLevel)
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun isManagedPaymentType(type: String): Boolean =
    type == BillingSupportedType.INAPP.name || type == BillingSupportedType.INAPP_SUBSCRIPTION.name

  /**
   * Flatten logic
   */

  fun finish(uid: String?) {
    sendPaymentEvent()
    sendRevenueEvent()
    sendPaymentSuccessEvent()
    val bundle = successBundle(uid)
    bundle.putString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    )
    navigator.navigate(_Finish(bundle))
  }

  fun finish(purchase: Purchase, orderReference: String?) {
    sendPaymentEvent()
    sendRevenueEvent()
    sendPaymentSuccessEvent()
    val bundle = mapPurchase(purchase, orderReference)
    bundle.putString(
      InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
      PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    )
    navigator.navigate(_Finish(bundle))
  }

  private fun successBundle(uid: String?): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    bundle.putString(TRANSACTION_HASH, uid)
    return bundle
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

  private fun pay(
    sku: String?,
    amount: BigDecimal,
    developerAddress: String,
    packageName: String,
    origin: String?,
    type: String,
    payload: String?,
    callbackUrl: String?,
    orderReference: String?,
    referrerUrl: String?,
    productToken: String?
  ): Completable {
    return getAttributionEntity(packageName)
      .flatMapCompletable { attrEntity ->
        pay(
          amount = amount,
          origin = origin,
          sku = sku,
          type = type,
          developerAddress = developerAddress,
          entityOemId = attrEntity.oemId,
          entityDomainId = attrEntity.domain,
          packageName = packageName,
          payload = payload,
          callbackUrl = callbackUrl,
          orderReference = orderReference,
          referrerUrl = referrerUrl,
          productToken = productToken
        )
      }
  }

  private fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
  }

  private fun pay(
    amount: BigDecimal,
    origin: String?,
    sku: String?,
    type: String,
    developerAddress: String,
    entityOemId: String?,
    entityDomainId: String?,
    packageName: String,
    payload: String?,
    callbackUrl: String?,
    orderReference: String?,
    referrerUrl: String?,
    productToken: String?
  ): Completable {
    return cache.save(
      getKey(amount.toString(), sku, packageName),
      Transaction(
        sku = sku,
        type = type,
        developerAddress = developerAddress,
        entityOemId = entityOemId,
        entityDomain = entityDomainId,
        packageName = packageName,
        amount = amount,
        origin = origin,
        status = Transaction.Status.PENDING,
        txId = null,
        purchaseUid = null,
        payload = payload,
        callback = callbackUrl,
        orderReference = orderReference,
        referrerUrl = referrerUrl,
        productToken = productToken
      )
    )
  }

  private fun getKey(amount: String? = "", sku: String? = "", packageName: String): String =
    amount + sku + packageName

  private fun getPaymentStatus(
    packageName: String, sku: String?,
    amount: BigDecimal
  ): Observable<RewardPayment> {
    return getPayment(packageName, sku, amount.toString())
      .flatMap { map(it) }
  }

  private fun getPayment(
    packageName: String, sku: String? = "",
    amount: String? = ""
  ): Observable<Transaction> =
    cache.get(getKey(amount, sku, packageName))
      .filter { it.status != Transaction.Status.PENDING }

  private fun map(transaction: Transaction): Observable<RewardPayment> {
    return when (transaction.status) {
      Transaction.Status.PROCESSING -> Observable.just(
        RewardPayment(transaction.orderReference, Status.PROCESSING)
      )
      Transaction.Status.COMPLETED -> Observable.just(
        RewardPayment(
          orderReference = transaction.orderReference,
          status = Status.COMPLETED,
          purchaseUid = transaction.purchaseUid
        )
      )
      Transaction.Status.ERROR -> Observable.just(
        RewardPayment(
          orderReference = transaction.orderReference,
          status = Status.ERROR,
          errorCode = transaction.errorCode,
          errorMessage = transaction.errorMessage
        )
      )
      Transaction.Status.FORBIDDEN -> Observable.just(
        RewardPayment(transaction.orderReference, Status.FORBIDDEN)
      )
      Transaction.Status.SUB_ALREADY_OWNED -> Observable.just(
        RewardPayment(transaction.orderReference, Status.SUB_ALREADY_OWNED)
      )
      Transaction.Status.NO_NETWORK -> Observable.just(
        RewardPayment(transaction.orderReference, Status.NO_NETWORK)
      )
      else -> throw UnsupportedOperationException(
        "Transaction status " + transaction.status + " not supported"
      )
    }
  }

  private fun getPaymentCompleted(
    packageName: String, sku: String?, purchaseUid: String?,
    billingType: BillingSupportedType
  ): Single<Purchase> {
    return getSkuPurchase(packageName, sku, purchaseUid, Schedulers.io(), billingType)
  }

  private fun getSkuPurchase(
    merchantName: String, sku: String?, purchaseUid: String?,
    scheduler: Scheduler, type: BillingSupportedType
  ): Single<Purchase> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        getSkuPurchase(merchantName, sku, purchaseUid, it.address, it.signedAddress, type)
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

  @Throws(Exception::class)
  private fun sign(plainText: String, ecKey: ECKey): String =
    ecKey.sign(HashUtil.sha3(plainText.toByteArray())).toHex()


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
      getSkuPurchaseSubs(
        packageName,
        purchaseUid!!,
        walletAddress,
        walletSignature
      )
    }

  private fun getSkuPurchase(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    inappBdsApi.getPurchases(
      packageName,
      walletAddress,
      walletSignature,
      type = BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT),
      sku = skuId
    )
      .map {
        if (it.items.isEmpty()) {
          throw HttpException(
            Response.error<GetPurchasesResponse>(
              404,
              "{}".toResponseBody("application/json".toMediaType())
            )
          )
        }
        map(packageName, it)[0]
      }

  private fun map(packageName: String, purchasesResponse: GetPurchasesResponse): List<Purchase> =
    purchasesResponse.items.map { map(packageName, it) }

  private fun map(packageName: String, inAppPurchaseResponse: InappPurchaseResponse): Purchase = Purchase(
    inAppPurchaseResponse.uid,
    RemoteProduct(inAppPurchaseResponse.sku),
    mapPurchaseState(inAppPurchaseResponse.state),
    false,
    null,
    Package(packageName),
    Signature(inAppPurchaseResponse.verification.signature, inAppPurchaseResponse.verification.data)
  )

  private fun mapPurchaseState(state: PurchaseState): State {
    return when (state) {
      PurchaseState.CONSUMED -> State.CONSUMED
      PurchaseState.PENDING -> State.PENDING
      PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }

  private fun getSkuPurchaseSubs(
    packageName: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    subsApi.getPurchase(packageName, purchaseUid, walletAddress, walletSignature)
      .map { map(packageName, it) }

  private fun map(
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
        subscriptionPurchaseResponse.verification.signature,
        subscriptionPurchaseResponse.verification.data
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

  private fun removeAsyncLocalPayment() {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.remove(LOCAL_PAYMENT_METHOD_KEY)
    editor.apply()
  }

  private fun getTransaction(
    packageName: String, sku: String?,
    amount: BigDecimal
  ): Observable<Transaction> {
    return getPayment(packageName, sku, amount.toString())
  }

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


  private fun showSupport(gamificationLevel: Int): Completable {
    return getWalletAddress()
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable { showSupport(it, gamificationLevel) }
      .subscribeOn(Schedulers.io())
  }

  private fun showSupport(walletAddress: String, gamificationLevel: Int): Completable {
    return Completable.fromAction {
      registerUser(gamificationLevel, walletAddress)
      displayChatScreen()
    }
  }

  private fun registerUser(level: Int, walletAddress: String) {
    // force lowercase to make sure 2 users are not registered with the same wallet address, where
    // one has uppercase letters (to be check summed), and the other does not
    val address = walletAddress.lowercase(Locale.ROOT)
    val currentUser = supportRepository.getCurrentUser()
    if (currentUser.userAddress != address || currentUser.gamificationLevel != level) {
      if (currentUser.userAddress != address) {
        Intercom.client().logout()
      }
      supportRepository.saveNewUser(address, level)
    }
  }

  private fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
      .displayMessenger()
  }

  private fun getWalletAddress(): Single<String> = find()
    .map { Keys.toChecksumAddress(it.address) }

  private fun convertToFiat(appcValue: Double, currency: String): Single<FiatValue> {
    return getTokenValue(currency)
      .map { fiatValueConversion -> calculateValue(fiatValueConversion, appcValue) }
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

  companion object {
    private val TAG = _AppcoinsRewardsBuyLogic::class.java.name

    private const val WALLET_VERIFIED = "wallet_verified_cc_"

    private const val TRANSACTION_HASH = "transaction_hash"
    private const val LOCAL_PAYMENT_METHOD_KEY = "LOCAL_PAYMENT_METHOD_KEY"
  }
}