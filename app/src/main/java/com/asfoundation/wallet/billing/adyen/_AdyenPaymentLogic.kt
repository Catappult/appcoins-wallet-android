package com.asfoundation.wallet.billing.adyen

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType.Companion.valueOfInsensitive
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.billing.ErrorInfo
import com.appcoins.wallet.billing.ErrorInfo.ErrorType
import com.appcoins.wallet.billing.adyen.*
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.REDIRECT
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentModel.Status.*
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.repository.ResponseErrorBaseBody
import com.appcoins.wallet.billing.skills.SkillsPaymentRepository
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.billing.util.getErrorCodeAndMessage
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.address.BillingAddressFragment
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.address.BillingAddressRepository
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.CVC_DECLINED
import com.asfoundation.wallet.billing.adyen.AdyenErrorCodeMapper.Companion.FRAUD
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import com.asfoundation.wallet.entity.AppcToFiatResponseBody
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.SignDataStandardNormalizer
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.TokenRateService.TokenToFiatApi
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.WalletUtils
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
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
import org.json.JSONObject
import org.web3j.crypto.Keys
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class _AdyenPaymentLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val returnUrl: String,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics,
  private val origin: String?,
  private val transactionBuilder: TransactionBuilder,
  private val paymentType: String,
  private val amount: BigDecimal,
  private val currency: String,
  private val skills: Boolean,
  private val isPreSelected: Boolean,
  private val gamificationLevel: Int,
  private val formatter: CurrencyFormatUtils,
  private val logger: Logger,
  private val accountKeystoreService: AccountKeystoreService,
  private val pref: SharedPreferences,
  private val analyticsSetUp: AnalyticsSetup,
  private val adyenApi: AdyenPaymentRepository.AdyenApi,
  private val supportRepository: SupportRepository,
  private val adyenSerializer: AdyenSerializer,
  private val gson: Gson,
  private val billingAddressRepository: BillingAddressRepository,
  private val passwordStore: PasswordStore,
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val promoCodeRepository: PromoCodeRepository,
  private val brokerBdsApi: RemoteRepository.BrokerBdsApi,
  private val subsApi: SubscriptionBillingApi,
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationRepository.BrokerVerificationApi,
  private val tokenToFiatApi: TokenToFiatApi,
  private val inappBdsApi: RemoteRepository.InappBdsApi,
  private val skillsPaymentRepository: SkillsPaymentRepository,
) {

  private var waitingResult = false
  private var cachedUid = ""
  private var cachedPaymentData: String? = null

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: android.util.Pair<String, ECKey>? = null

  private var billingAddressModel: BillingAddressModel? = null

  fun present(savedInstanceState: Bundle?) {
    retrieveSavedInstace(savedInstanceState)
    if (!waitingResult) loadPaymentMethodInfo()
  }

  private fun onSupportClicks() {
    showSupport(gamificationLevel)
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun onForgetCardClick() {
    view.setState(_LoadingViewState)
    disablePayments()
      .toObservable()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { success -> if (!success) view.setState(_ErrorViewState()) }
      .filter { it }
      .observeOn(Schedulers.io())
      .flatMapSingle {
        loadPaymentInfo(mapPaymentToService(paymentType), amount.toString(), currency)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSuccess {
            billingAddressRepository.forgetBillingAddress()
            view.setState(_LoadedViewState)
            if (it.error.hasError) {
              if (it.error.isNetworkError) view.setState(_NetworkErrorViewState)
              else view.setState(_ErrorViewState())
            } else {
              view.setState(_SetCardDataViewState(it, true))
            }
          }
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorViewState())
      })
      .isDisposed
  }

  private fun loadPaymentMethodInfo() {
    view.setState(_LoadingViewState)
    loadPaymentInfo(mapPaymentToService(paymentType), amount.toString(), currency)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        if (it.error.hasError) {
          sendPaymentErrorEvent(it.error.errorInfo?.httpCode, it.error.errorInfo?.text)
          view.setState(_LoadedViewState)
          handleErrors(it.error)
        } else {
          val amount = formatter.formatPaymentCurrency(it.priceAmount, WalletCurrency.FIAT)
          view.setState(_SetProductPriceViewState(amount, it.priceCurrency))
          if (paymentType == PaymentType.CARD.name) {
            view.setState(_LoadedViewState)
            sendPaymentMethodDetailsEvent(BillingAnalytics.PAYMENT_METHOD_CC)
            view.setState(
              _SetCardDataViewState(it, false) { cardWrapper ->
                onBuyClick(cardWrapper, it.priceAmount, it.priceCurrency)
              }
            )
            paymentAnalytics.stopTimingForTotalEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
          } else if (paymentType == PaymentType.PAYPAL.name) {
            launchPaypal(it.paymentMethod!!, it.priceAmount, it.priceCurrency)
          }
        }
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorViewState())
      })
      .isDisposed
  }

  private fun launchPaypal(
    paymentMethodInfo: ModelObject,
    priceAmount: BigDecimal,
    priceCurrency: String
  ) {
    makePayment(
      adyenPaymentMethod = paymentMethodInfo,
      shouldStoreMethod = false,
      hasCvc = false,
      supportedShopperInteraction = emptyList(),
      returnUrl = returnUrl,
      value = priceAmount.toString(),
      currency = priceCurrency,
      reference = transactionBuilder.orderReference,
      paymentType = mapPaymentToService(paymentType).transactionType,
      origin = origin,
      packageName = transactionBuilder.domain,
      metadata = transactionBuilder.payload,
      sku = transactionBuilder.skuId,
      callbackUrl = transactionBuilder.callbackUrl,
      transactionType = transactionBuilder.type,
      developerWallet = transactionBuilder.toAddress(),
      referrerUrl = transactionBuilder.referrerUrl
    )
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .filter { !waitingResult }
      .doOnSuccess {
        view.setState(_LoadedViewState)
        handlePaymentModel(it)
      }
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorViewState())
      })
      .isDisposed
  }

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      handleErrors(paymentModel.error)
    } else {
      view.setState(_LoadingViewState)
      sendPaymentMethodDetailsEvent(mapPaymentToAnalytics(paymentType))
      paymentAnalytics.stopTimingForTotalEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_PP)
      paymentAnalytics.startTimingForPurchaseEvent()
      handleAdyenAction(paymentModel)
    }
  }

  private fun onBillingAddressResult(data: Intent) {
    this.billingAddressModel =
      data.getSerializableExtra(BillingAddressFragment.BILLING_ADDRESS_MODEL) as BillingAddressModel
  }

  private fun onBuyClick(
    adyenWrapper: AdyenCardWrapper,
    priceAmount: BigDecimal,
    priceCurrency: String
  ) {
    Observable.fromCallable {
      paymentAnalytics.startTimingForPurchaseEvent()
      adyenWrapper
    }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        view.setState(_LoadingViewState)
      }
      .observeOn(Schedulers.io())
      .flatMapSingle { adyenCard ->
        handleBuyAnalytics(transactionBuilder)
        val shouldStore = billingAddressModel?.remember ?: adyenCard.shouldStoreCard
        if (skills) {
          makeSkillsPayment(
            returnUrl = returnUrl,
            productToken = transactionBuilder.productToken,
            encryptedCardNumber = adyenCard.cardPaymentMethod.encryptedCardNumber,
            encryptedExpiryMonth = adyenCard.cardPaymentMethod.encryptedExpiryMonth,
            encryptedExpiryYear = adyenCard.cardPaymentMethod.encryptedExpiryYear,
            encryptedSecurityCode = adyenCard.cardPaymentMethod.encryptedSecurityCode!!
          )
        } else {
          makePayment(
            adyenPaymentMethod = adyenCard.cardPaymentMethod,
            shouldStoreMethod = shouldStore,
            hasCvc = adyenCard.hasCvc,
            supportedShopperInteraction = adyenCard.supportedShopperInteractions,
            returnUrl = returnUrl,
            value = priceAmount.toString(),
            currency = priceCurrency,
            reference = transactionBuilder.orderReference,
            paymentType = mapPaymentToService(paymentType).transactionType,
            origin = origin,
            packageName = transactionBuilder.domain,
            metadata = transactionBuilder.payload,
            sku = transactionBuilder.skuId,
            callbackUrl = transactionBuilder.callbackUrl,
            transactionType = transactionBuilder.type,
            developerWallet = transactionBuilder.toAddress(),
            referrerUrl = transactionBuilder.referrerUrl,
            billingAddress = mapToAdyenBillingAddress(billingAddressModel)
          )
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable { handlePaymentResult(it, priceAmount, priceCurrency) }
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorViewState())
      })
      .isDisposed
  }

  private fun handlePaymentResult(
    paymentModel: PaymentModel,
    priceAmount: BigDecimal? = null,
    priceCurrency: String? = null
  ): Completable = when {
    paymentModel.resultCode.equals("AUTHORISED", true) -> {
      getAuthorisedTransaction(paymentModel.uid)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .flatMapCompletable {
          when {
            it.status == COMPLETED -> {
              sendPaymentSuccessEvent()
              createBundle(it.hash, it.orderReference, it.purchaseUid)
                .doOnSuccess {
                  sendPaymentEvent()
                  sendRevenueEvent()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapCompletable { bundle -> handleSuccessTransaction(bundle) }
            }
            isPaymentFailed(it.status) -> {
              if (paymentModel.status == FAILED && paymentType == PaymentType.PAYPAL.name) {
                retrieveFailedReason(paymentModel.uid)
              } else {
                Completable.fromAction {
                  sendPaymentErrorEvent(
                    it.error.errorInfo?.httpCode,
                    buildRefusalReason(it.status, it.error.errorInfo?.text)
                  )
                  handleErrors(it.error)
                }
                  .subscribeOn(AndroidSchedulers.mainThread())
              }
            }
            else -> {
              sendPaymentErrorEvent(
                it.error.errorInfo?.httpCode,
                it.status.toString() + it.error.errorInfo?.text
              )
              Completable.fromAction { handleErrors(it.error) }
            }
          }
        }
    }
    paymentModel.status == PENDING_USER_PAYMENT && paymentModel.action != null -> {
      Completable.fromAction {
        view.setState(_LoadingViewState)
        handleAdyenAction(paymentModel)
      }
    }
    paymentModel.refusalReason != null -> Completable.fromAction {
      var riskRules: String? = null
      paymentModel.refusalCode?.let { code ->
        when (code) {
          CVC_DECLINED -> view.setState(_CvvErrorViewState)
          FRAUD -> {
            handleFraudFlow(mapAdyenErrorCode(code), paymentModel.fraudResultIds)
            riskRules = paymentModel.fraudResultIds.sorted()
              .joinToString(separator = "-")
          }
          else -> view.setState(_ErrorCodeViewState(mapAdyenErrorCode(code)))
        }
      }
      sendPaymentErrorEvent(paymentModel.refusalCode, paymentModel.refusalReason, riskRules)
    }
    paymentModel.error.hasError -> Completable.fromAction {
      if (isBillingAddressError(paymentModel.error, priceAmount, priceCurrency)) {
        view.setState(_BillingAddressViewState(priceAmount!!, priceCurrency!!))
      } else {
        sendPaymentErrorEvent(
          paymentModel.error.errorInfo?.httpCode,
          paymentModel.error.errorInfo?.text
        )
        handleErrors(paymentModel.error)
      }
    }
    paymentModel.status == FAILED && paymentType == PaymentType.PAYPAL.name -> {
      retrieveFailedReason(paymentModel.uid)
    }
    paymentModel.status == CANCELED -> Completable.fromAction {
      view.setState(_PaymentMethodsViewState)
    }
    else -> Completable.fromAction {
      sendPaymentErrorEvent(
        paymentModel.error.errorInfo?.httpCode,
        "${paymentModel.status} ${paymentModel.error.errorInfo?.text}"
      )
      view.setState(_ErrorViewState())
    }
  }

  private fun isBillingAddressError(
    error: Error,
    priceAmount: BigDecimal?,
    priceCurrency: String?
  ): Boolean =
    error.errorInfo?.errorType == ErrorType.BILLING_ADDRESS && priceAmount != null && priceCurrency != null

  private fun handleSuccessTransaction(purchaseBundleModel: PurchaseBundleModel): Completable =
    Completable.fromAction { view.setState(_SuccessViewState(purchaseBundleModel.renewal)) }
      .andThen(Completable.fromAction { navigator.navigate(_Finish(purchaseBundleModel.bundle)) })

  private fun retrieveFailedReason(uid: String): Completable =
    getFailedTransactionReason(uid)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable {
        Completable.fromAction {
          sendPaymentErrorEvent(it.errorCode, it.errorMessage ?: "")
          if (it.errorCode != null) {
            view.setState(_ErrorCodeViewState(mapAdyenErrorCode(it.errorCode!!)))
          } else {
            view.setState(_ErrorViewState())
          }
        }
      }

  @Suppress("UNUSED_PARAMETER")
  private fun handleFraudFlow(@StringRes error: Int, fraudCheckIds: List<Int>) {
    isWalletVerified()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { verified ->
        view.setState(_VerificationErrorViewState(verified))
      }
      .subscribe({}, { view.setState(_ErrorCodeViewState(error)) })
      .isDisposed
  }

  private fun onVerificationClick(verified: Boolean) {
    view.setState(_VerificationViewState(verified))
  }

  private fun buildRefusalReason(status: PaymentModel.Status, message: String?): String =
    message?.let { "$status : $it" } ?: status.toString()

  private fun isPaymentFailed(status: PaymentModel.Status): Boolean =
    status == FAILED || status == CANCELED || status == INVALID_TRANSACTION || status == PaymentModel.Status.FRAUD

  private fun onPaymentDetails(response: AdyenComponentResponseModel) {
    Observable.fromCallable { response }
      .observeOn(Schedulers.io())
      .flatMapSingle {
        submitRedirect(
          uid = cachedUid,
          details = convertToJson(it.details!!),
          paymentData = it.paymentData ?: cachedPaymentData
        )
      }
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable { handlePaymentResult(it) }
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ErrorViewState())
      })
      .isDisposed
  }

  //This method is used to avoid the nameValuePairs key problem that occurs when we pass a JSONObject trough a GSON converter
  private fun convertToJson(details: JSONObject): JsonObject {
    val json = JsonObject()
    val keys = details.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      val value = details.get(key)
      if (value is String) json.addProperty(key, value)
    }
    return json
  }

  private fun on3DSErrors(errorMessage: String) {
    Observable.fromCallable { errorMessage }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        if (it == CHALLENGE_CANCELED) view.setState(_PaymentMethodsViewState)
        else {
          logger.log(TAG, it)
          view.setState(_ErrorViewState())
        }
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
    outState.putString(UID, cachedUid)
    outState.putString(PAYMENT_DATA, cachedPaymentData)
  }

  private fun retrieveSavedInstace(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      waitingResult = it.getBoolean(WAITING_RESULT)
      cachedUid = it.getString(UID, "")
      cachedPaymentData = it.getString(PAYMENT_DATA)
    }
  }

  private fun sendPaymentMethodDetailsEvent(paymentMethod: String) {
    Single.just(transactionBuilder)
      .observeOn(Schedulers.io())
      .doOnSuccess { transactionBuilder ->
        analytics.sendPaymentMethodDetailsEvent(
          transactionBuilder.domain,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          paymentMethod,
          transactionBuilder.type
        )
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun onErrorDismissEvent() {
    navigator.navigate(_Close(Bundle()))
  }

  private fun onBack() {
    Single.just(transactionBuilder)
      .observeOn(Schedulers.io())
      .doOnSuccess { handlePaymentMethodAnalytics(it) }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun handlePaymentMethodAnalytics(transaction: TransactionBuilder) {
    if (isPreSelected) {
      analytics.sendPreSelectedPaymentMethodEvent(
        transactionBuilder.domain,
        transaction.skuId,
        transaction.amount().toString(),
        mapPaymentToService(paymentType).transactionType,
        transaction.type,
        "cancel"
      )
      navigator.navigate(_Close(mapCancellation()))
    } else {
      analytics.sendPaymentConfirmationEvent(
        transactionBuilder.domain,
        transaction.skuId,
        transaction.amount().toString(),
        mapPaymentToService(paymentType).transactionType,
        transaction.type,
        "back"
      )
      view.setState(_PaymentMethodsViewState)
    }
  }

  private fun onMorePaymentsClick() {
    if (isPreSelected)
      Single.just(transactionBuilder)
        .observeOn(Schedulers.io())
        .doOnSuccess { onMorePaymentsAnalytics(it) }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSuccess { showMoreMethods() }
        .subscribe({}, { it.printStackTrace() })
        .isDisposed
  }

  private fun onMorePaymentsAnalytics(transaction: TransactionBuilder): Unit =
    analytics.sendPreSelectedPaymentMethodEvent(
      transactionBuilder.domain,
      transaction.skuId,
      transaction.amount().toString(),
      mapPaymentToService(paymentType).transactionType,
      transaction.type,
      "other_payments"
    )

  private fun onRedirectResponse(uri: Uri) {
    view.setState(_SubmitUriResultViewState(uri))
  }

  private fun showMoreMethods() {
    removePreSelectedPaymentMethod()
    view.setState(_PaymentMethodsViewState)
  }

  private fun sendPaymentEvent() {
    Single.just(transactionBuilder)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { transactionBuilder: TransactionBuilder ->
        stopTimingForPurchaseEvent(true)
        analytics.sendPaymentEvent(
          transactionBuilder.domain,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          mapPaymentToAnalytics(paymentType),
          transactionBuilder.type
        )
      }
      .isDisposed
  }

  private fun sendRevenueEvent() {
    Single.just(transactionBuilder)
      .observeOn(Schedulers.io())
      .doOnSuccess { transactionBuilder ->
        analytics.sendRevenueEvent(
          convertToFiat(
            appcValue = transactionBuilder.amount().toDouble(),
            currency = BillingAnalytics.EVENT_REVENUE_CURRENCY
          )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .blockingGet()
            .amount
            .setScale(2, BigDecimal.ROUND_UP)
            .toString()
        )
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun sendPaymentSuccessEvent() {
    Single.just(transactionBuilder)
      .observeOn(Schedulers.io())
      .doOnSuccess { transaction ->
        analytics.sendPaymentSuccessEvent(
          transactionBuilder.domain,
          transaction.skuId,
          transaction.amount().toString(),
          mapPaymentToAnalytics(paymentType),
          transaction.type
        )
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun sendPaymentErrorEvent(
    refusalCode: Int?,
    refusalReason: String?,
    riskRules: String? = null
  ) {
    Single.just(transactionBuilder)
      .observeOn(Schedulers.io())
      .doOnSuccess { transaction ->
        stopTimingForPurchaseEvent(false)
        analytics.sendPaymentErrorWithDetailsAndRiskEvent(
          transactionBuilder.domain,
          transaction.skuId,
          transaction.amount().toString(),
          mapPaymentToAnalytics(paymentType),
          transaction.type,
          refusalCode.toString(),
          refusalReason,
          riskRules
        )
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun mapPaymentToAnalytics(paymentType: String): String =
    if (paymentType == PaymentType.CARD.name) {
      BillingAnalytics.PAYMENT_METHOD_CC
    } else {
      BillingAnalytics.PAYMENT_METHOD_PAYPAL
    }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods =
    if (paymentType == PaymentType.CARD.name) {
      AdyenPaymentRepository.Methods.CREDIT_CARD
    } else {
      AdyenPaymentRepository.Methods.PAYPAL
    }

  private fun mapToAdyenBillingAddress(billingAddressModel: BillingAddressModel?): AdyenBillingAddress? =
    billingAddressModel?.let {
      AdyenBillingAddress(it.address, it.city, it.zipcode, it.number, it.state, it.country)
    }

  private fun createBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?
  ): Single<PurchaseBundleModel> =
    getCompletedPurchaseBundle(
      type = transactionBuilder.type,
      merchantName = transactionBuilder.domain,
      sku = transactionBuilder.skuId,
      purchaseUid = purchaseUid,
      orderReference = orderReference,
      hash = hash,
      scheduler = Schedulers.io()
    )
      .map { mapPaymentMethodId(it) }

  private fun mapPaymentMethodId(purchaseBundleModel: PurchaseBundleModel): PurchaseBundleModel {
    val bundle = purchaseBundleModel.bundle
    if (paymentType == PaymentType.CARD.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id
      )
    } else if (paymentType == PaymentType.PAYPAL.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.PAYPAL.id
      )
    }
    return PurchaseBundleModel(bundle, purchaseBundleModel.renewal)
  }

  private fun handleBuyAnalytics(transactionBuilder: TransactionBuilder) = if (isPreSelected) {
    analytics.sendPreSelectedPaymentMethodEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      mapPaymentToService(paymentType).transactionType,
      transactionBuilder.type,
      "buy"
    )
  } else {
    analytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      mapPaymentToService(paymentType).transactionType,
      transactionBuilder.type,
      "buy"
    )
  }

  private fun onAdyenErrorBack() {
    if (isPreSelected) {
      navigator.navigate(_Close(mapCancellation()))
    } else {
      view.setState(_PaymentMethodsViewState)
    }
  }

  private fun onAdyenErrorCancel() {
    navigator.navigate(_Close(mapCancellation()))
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      when (val type = paymentModel.action?.type) {
        REDIRECT -> {
          cachedPaymentData = paymentModel.paymentData
          cachedUid = paymentModel.uid
          navigator.navigate(_GoToUriForResult(paymentModel.redirectUrl!!))
          waitingResult = true
        }
        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE -> {
          cachedUid = paymentModel.uid
          view.setState(_Handle3DSActionViewState(paymentModel.action!!))
          waitingResult = true
        }
        else -> {
          logger.log(TAG, "Unknown adyen action: $type")
          view.setState(_ErrorViewState())
        }
      }
    }
  }

  /**
   * Flatten logic
   */

  private fun handleErrors(error: Error) {
    when {
      error.isNetworkError -> view.setState(_NetworkErrorViewState)

      error.errorInfo?.errorType == ErrorType.INVALID_CARD -> view.setState(
        _InvalidCardErrorViewState
      )

      error.errorInfo?.errorType == ErrorType.CARD_SECURITY_VALIDATION -> view.setState(
        _SecurityValidationErrorViewState
      )

      error.errorInfo?.errorType == ErrorType.OUTDATED_CARD -> view.setState(
        _OutdatedCardErrorViewState
      )

      error.errorInfo?.errorType == ErrorType.ALREADY_PROCESSED -> view.setState(
        _AlreadyProcessedErrorViewState
      )

      error.errorInfo?.errorType == ErrorType.PAYMENT_ERROR -> view.setState(_PaymentErrorViewState)

      error.errorInfo?.httpCode != null -> {
        val resId = mapError(error.errorInfo?.errorType)
        if (error.errorInfo?.httpCode == HTTP_FRAUD_CODE) handleFraudFlow(resId, emptyList())
        else view.setState(_ErrorCodeViewState(resId))
      }
      else -> view.setState(_ErrorViewState())
    }
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    val paymentMethod = when (paymentType) {
      PaymentType.PAYPAL.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_PP
      PaymentType.CARD.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_CC
      else -> return
    }
    paymentAnalytics.stopTimingForPurchaseEvent(paymentMethod, success, isPreSelected)
  }

  /**
   * Logic
   */

  private fun disablePayments() = getWalletAddress()
    .flatMap { disablePayments(it) }

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


  private fun disablePayments(walletAddress: String): Single<Boolean> {
    return adyenApi.disablePayments(AdyenPaymentRepository.DisableWallet(walletAddress))
      .toSingleDefault(true)
      .doOnError { it.printStackTrace() }
      .onErrorReturn {
        false
      }
  }

  private fun showSupport(gamificationLevel: Int): Completable = getWalletAddress()
    .observeOn(AndroidSchedulers.mainThread())
    .flatMapCompletable { showSupport(it, gamificationLevel) }
    .subscribeOn(Schedulers.io())

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

  private fun loadPaymentInfo(
    methods: AdyenPaymentRepository.Methods,
    value: String,
    currency: String
  ): Single<PaymentInfoModel> = getWalletAddress()
    .flatMap { loadPaymentInfo(methods, value, currency, it) }

  private fun loadPaymentInfo(
    methods: AdyenPaymentRepository.Methods,
    value: String,
    currency: String,
    walletAddress: String
  ): Single<PaymentInfoModel> =
    adyenApi.loadPaymentInfo(walletAddress, value, currency, methods.transactionType)
      .map { mapPaymentMethodsResponse(it, methods) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        mapInfoModelError(it)
      }

  private fun mapPaymentMethodsResponse(
    response: PaymentMethodsResponse,
    method: AdyenPaymentRepository.Methods
  ): PaymentInfoModel {
    //This was done due to the fact that using the PaymentMethodsApiResponse to map the response
    // directly with retrofit was breaking when the response came with a configuration object
    // since the Adyen lib considers configuration a string.
    val adyenResponse: PaymentMethodsApiResponse =
      adyenSerializer.deserializePaymentMethods(response)
    return adyenResponse.storedPaymentMethods
      ?.find { it.type == method.adyenType }
      ?.let { PaymentInfoModel(it, response.price.value, response.price.currency) }
      ?: adyenResponse.paymentMethods
        ?.find { it.type == method.adyenType }
        ?.let { PaymentInfoModel(it, response.price.value, response.price.currency) }
      ?: PaymentInfoModel(Error(true))
  }

  private fun mapInfoModelError(throwable: Throwable): PaymentInfoModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    val errorInfo = mapErrorInfo(codeAndMessage.first, codeAndMessage.second)
    return PaymentInfoModel(Error(true, throwable.isNoNetworkException(), errorInfo))
  }

  private fun mapErrorInfo(httpCode: Int?, message: String?): ErrorInfo {
    val messageGson = gson.fromJson(message, ResponseErrorBaseBody::class.java)
    val errorType = getErrorType(httpCode, messageGson.code, messageGson.text, messageGson.data)
    return ErrorInfo(httpCode, messageGson.code, messageGson.text, errorType)
  }

  private fun getErrorType(
    httpCode: Int?,
    messageCode: String?,
    text: String?,
    data: Any?
  ): ErrorType = when {
    httpCode != null && httpCode == 400 && messageCode == FIELDS_MISSING_CODE
        && text?.contains("payment.billing") == true -> ErrorType.BILLING_ADDRESS
    messageCode == NOT_ALLOWED_CODE -> ErrorType.SUB_ALREADY_OWNED
    messageCode == FORBIDDEN_CODE -> ErrorType.BLOCKED
    httpCode == CONFLICT_HTTP_CODE -> ErrorType.CONFLICT
    messageCode == ADYEN_V2_ERROR && (data is Number) -> {
      when (data.toInt()) {
        101 -> ErrorType.INVALID_CARD
        105 -> ErrorType.CARD_SECURITY_VALIDATION
        172, 174, 422, 800 -> ErrorType.OUTDATED_CARD
        704 -> ErrorType.ALREADY_PROCESSED
        905 -> ErrorType.PAYMENT_ERROR
        else -> ErrorType.UNKNOWN
      }
    }
    else -> ErrorType.UNKNOWN
  }

  private fun makePayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    hasCvc: Boolean,
    supportedShopperInteraction: List<String>,
    returnUrl: String,
    value: String,
    currency: String,
    reference: String?,
    paymentType: String,
    origin: String?,
    packageName: String,
    metadata: String?,
    sku: String,
    callbackUrl: String?,
    transactionType: String,
    developerWallet: String?,
    referrerUrl: String?,
    billingAddress: AdyenBillingAddress? = null
  ): Single<PaymentModel> {
    return Single.zip(
      getAndSignCurrentWalletAddress(),
      getAttributionEntity(packageName)
    ) { address, attributionEntity -> Pair(address, attributionEntity) }
      .flatMap { pair ->
        val addressModel = pair.first
        val attrEntity = pair.second
        getCurrentPromoCode()
          .flatMap { promoCode ->
            makePayment(
              adyenPaymentMethod = adyenPaymentMethod,
              shouldStoreMethod = shouldStoreMethod,
              hasCvc = hasCvc,
              supportedShopperInteractions = supportedShopperInteraction,
              returnUrl = returnUrl,
              value = value,
              currency = currency,
              reference = reference,
              paymentType = paymentType,
              walletAddress = addressModel.address,
              origin = origin,
              packageName = packageName,
              metadata = metadata,
              sku = sku,
              callbackUrl = callbackUrl,
              transactionType = transactionType,
              developerWallet = developerWallet,
              entityOemId = attrEntity.oemId,
              entityDomain = attrEntity.domain,
              entityPromoCode = promoCode.code,
              userWallet = addressModel.address,
              walletSignature = addressModel.signedAddress,
              billingAddress = billingAddress,
              referrerUrl = referrerUrl
            )
          }
      }
  }

  private fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKey(wallet)
        .map { sign(normalizer.normalize(Keys.toChecksumAddress(wallet.address)), it) }
        .map { WalletAddressModel(wallet.address, it) }
    }

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

  private fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
  }

  private fun getCurrentPromoCode(): Single<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
      .firstOrError()
  }

  private fun makePayment(
    adyenPaymentMethod: ModelObject,
    shouldStoreMethod: Boolean,
    hasCvc: Boolean,
    supportedShopperInteractions: List<String>,
    returnUrl: String,
    value: String,
    currency: String,
    reference: String?,
    paymentType: String,
    walletAddress: String,
    origin: String?,
    packageName: String,
    metadata: String?,
    sku: String,
    callbackUrl: String?,
    transactionType: String,
    developerWallet: String?,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    walletSignature: String,
    billingAddress: AdyenBillingAddress?,
    referrerUrl: String?
  ): Single<PaymentModel> {
    val shopperInteraction = if (!hasCvc && supportedShopperInteractions.contains("ContAuth")) {
      "ContAuth"
    } else "Ecommerce"
    return if (transactionType == BillingSupportedType.INAPP_SUBSCRIPTION.name) {
      subsApi.getSkuSubscriptionToken(
        domain = packageName,
        sku = sku,
        currency = currency,
        walletAddress = walletAddress,
        walletSignature = walletSignature
      )
        .map {
          TokenPayment(
            adyenPaymentMethod = adyenPaymentMethod,
            shouldStoreMethod = shouldStoreMethod,
            returnUrl = returnUrl,
            shopperInteraction = shopperInteraction,
            billingAddress = billingAddress,
            callbackUrl = callbackUrl,
            metadata = metadata,
            method = paymentType,
            origin = origin,
            reference = reference,
            developer = developerWallet,
            entityOemId = entityOemId,
            entityDomain = entityDomain,
            entityPromoCode = entityPromoCode,
            user = userWallet,
            referrerUrl = referrerUrl,
            token = it
          )
        }
        .flatMap { adyenApi.makeTokenPayment(walletAddress, walletSignature, it) }
        .map { mapAdyenTransactionResponse(it) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          mapPaymentModelError(it)
        }
    } else {
      return adyenApi.makePayment(
        walletAddress,
        walletSignature,
        AdyenPaymentRepository.Payment(
          adyenPaymentMethod = adyenPaymentMethod,
          shouldStoreMethod = shouldStoreMethod,
          returnUrl = returnUrl,
          shopperInteraction = shopperInteraction,
          billingAddress = billingAddress,
          callbackUrl = callbackUrl,
          domain = packageName,
          metadata = metadata,
          method = paymentType,
          origin = origin,
          sku = sku,
          reference = reference,
          type = transactionType,
          currency = currency,
          value = value,
          developer = developerWallet,
          entityOemId = entityOemId,
          entityDomain = entityDomain,
          entityPromoCode = entityPromoCode,
          user = userWallet,
          referrerUrl = referrerUrl
        )
      )
        .map { mapAdyenTransactionResponse(it) }
        .onErrorReturn {
          logger.log("AdyenPaymentRepository", it)
          mapPaymentModelError(it)
        }
    }
  }

  private fun mapAdyenTransactionResponse(response: AdyenTransactionResponse): PaymentModel {
    val adyenResponse = response.payment
    var actionType: String? = null
    var jsonAction: JsonObject? = null
    var redirectUrl: String? = null
    var action: Action? = null
    var fraudResultsId: List<Int> = emptyList()

    if (adyenResponse != null) {
      if (adyenResponse.fraudResult != null) {
        fraudResultsId = adyenResponse.fraudResult!!.results.map { it.fraudCheckResult.checkId }
      }
      if (adyenResponse.action != null) {
        actionType = adyenResponse.action!!.get("type")?.asString
        jsonAction = adyenResponse.action
      }
    }

    if (actionType != null && jsonAction != null) {
      when (actionType) {
        REDIRECT -> {
          action = adyenSerializer.deserializeRedirectAction(jsonAction)
          redirectUrl = action.url
        }
        THREEDS2 -> action = adyenSerializer.deserialize3DS(jsonAction)
        THREEDS2FINGERPRINT -> action = adyenSerializer.deserialize3DSFingerprint(jsonAction)
        THREEDS2CHALLENGE -> action = adyenSerializer.deserialize3DSChallenge(jsonAction)
      }
    }
    return PaymentModel(
      resultCode = adyenResponse?.resultCode,
      refusalReason = adyenResponse?.refusalReason,
      refusalCode = adyenResponse?.refusalReasonCode?.toInt(),
      action = action,
      redirectUrl = redirectUrl,
      paymentData = action?.paymentData,
      uid = response.uid,
      purchaseUid = null,
      hash = response.hash,
      orderReference = response.orderReference,
      fraudResultIds = fraudResultsId,
      status = mapTransactionStatus(response.status),
      errorMessage = response.metadata?.errorMessage,
      errorCode = response.metadata?.errorCode
    )
  }

  private fun mapPaymentModelError(throwable: Throwable): PaymentModel {
    throwable.printStackTrace()
    val codeAndMessage = throwable.getErrorCodeAndMessage()
    val errorInfo = mapErrorInfo(codeAndMessage.first, codeAndMessage.second)
    val error = Error(true, throwable.isNoNetworkException(), errorInfo)
    return PaymentModel(error)
  }

  private fun mapTransactionStatus(status: TransactionStatus): PaymentModel.Status {
    return when (status) {
      TransactionStatus.PENDING -> PENDING
      TransactionStatus.PENDING_SERVICE_AUTHORIZATION -> PENDING_SERVICE_AUTHORIZATION
      TransactionStatus.SETTLED -> SETTLED
      TransactionStatus.PROCESSING -> PROCESSING
      TransactionStatus.COMPLETED -> COMPLETED
      TransactionStatus.PENDING_USER_PAYMENT -> PENDING_USER_PAYMENT
      TransactionStatus.INVALID_TRANSACTION -> INVALID_TRANSACTION
      TransactionStatus.FAILED -> FAILED
      TransactionStatus.CANCELED -> CANCELED
      TransactionStatus.FRAUD -> PaymentModel.Status.FRAUD
      TransactionStatus.PENDING_VALIDATION -> PENDING
      TransactionStatus.PENDING_CODE -> PENDING
      TransactionStatus.VERIFIED -> COMPLETED
      TransactionStatus.EXPIRED -> FAILED
    }
  }

  private fun getAuthorisedTransaction(uid: String): Observable<PaymentModel> {
    return getAndSignCurrentWalletAddress()
      .flatMapObservable { walletAddressModel ->
        Observable.interval(0, 10, TimeUnit.SECONDS, Schedulers.io())
          .timeInterval()
          .switchMap {
            getTransaction(uid, walletAddressModel.address, walletAddressModel.signedAddress)
              .toObservable()
          }
          .filter { isEndingState(it.status) }
          .distinctUntilChanged { transaction -> transaction.status }
      }
  }

  private fun getTransaction(
    uid: String,
    walletAddress: String,
    signedWalletAddress: String
  ): Single<PaymentModel> {
    return brokerBdsApi.getAppcoinsTransaction(uid, walletAddress, signedWalletAddress)
      .map { mapTransaction(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        mapPaymentModelError(it)
      }
  }

  private fun mapTransaction(response: Transaction): PaymentModel {
    return PaymentModel(
      resultCode = "",
      refusalReason = null,
      refusalCode = null,
      action = null,
      redirectUrl = "",
      paymentData = "",
      uid = response.uid,
      purchaseUid = response.metadata?.purchaseUid,
      hash = response.hash,
      orderReference = response.orderReference,
      fraudResultIds = emptyList(),
      status = mapStatus(response.status)
    )
  }

  private fun mapStatus(status: Transaction.Status): PaymentModel.Status {
    return when (status) {
      Transaction.Status.PENDING -> PENDING
      Transaction.Status.PENDING_SERVICE_AUTHORIZATION -> PENDING_SERVICE_AUTHORIZATION
      Transaction.Status.SETTLED -> SETTLED
      Transaction.Status.PROCESSING -> PROCESSING
      Transaction.Status.COMPLETED -> COMPLETED
      Transaction.Status.PENDING_USER_PAYMENT -> PENDING_USER_PAYMENT
      Transaction.Status.INVALID_TRANSACTION -> INVALID_TRANSACTION
      Transaction.Status.FAILED -> FAILED
      Transaction.Status.CANCELED -> CANCELED
      Transaction.Status.FRAUD -> PaymentModel.Status.FRAUD
    }
  }

  private fun isEndingState(status: PaymentModel.Status): Boolean {
    return (status == COMPLETED
        || status == FAILED
        || status == CANCELED
        || status == INVALID_TRANSACTION
        || status == PaymentModel.Status.FRAUD)
  }

  private fun getFailedTransactionReason(uid: String, timesCalled: Int = 0): Single<PaymentModel> {
    return if (timesCalled < MAX_NUMBER_OF_TRIES) {
      getAndSignCurrentWalletAddress()
        .flatMap { walletAddressModel ->
          Single.zip(
            getTransaction(
              uid = uid,
              walletAddress = walletAddressModel.address,
              signedWalletAddress = walletAddressModel.signedAddress
            ), Single.timer(REQUEST_INTERVAL_IN_SECONDS, TimeUnit.SECONDS, Schedulers.io())
          ) { paymentModel: PaymentModel, _: Long -> paymentModel }
        }
        .flatMap {
          if (it.errorCode != null) Single.just(it)
          else getFailedTransactionReason(it.uid, timesCalled + 1)
        }
    } else {
      Single.just(PaymentModel(Error(true)))
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

  private fun submitRedirect(
    uid: String,
    details: JsonObject,
    paymentData: String?
  ): Single<PaymentModel> {
    return getAndSignCurrentWalletAddress()
      .flatMap {
        submitRedirect(uid, it.address, it.signedAddress, details, paymentData)
      }
  }


  private fun submitRedirect(
    uid: String,
    walletAddress: String,
    walletSignature: String,
    details: JsonObject,
    paymentData: String?
  ): Single<PaymentModel> {
    return adyenApi.submitRedirect(
      uid = uid,
      address = walletAddress,
      signature = walletSignature,
      payment = AdyenPaymentRepository.AdyenPayment(details, paymentData)
    )
      .map { mapAdyenTransactionResponse(it) }
      .onErrorReturn {
        logger.log("AdyenPaymentRepository", it)
        mapPaymentModelError(it)
      }
  }

  private fun mapCancellation(): Bundle = Bundle().apply {
    putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_USER_CANCELED)
  }

  private fun removePreSelectedPaymentMethod() {
    pref.edit()
      .remove(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY)
      .apply()
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

  private fun getCompletedPurchaseBundle(
    type: String,
    merchantName: String,
    sku: String?,
    purchaseUid: String?,
    orderReference: String?,
    hash: String?,
    scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    val billingType = valueOfInsensitive(type)
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
              404,
              "{}".toResponseBody("application/json".toMediaType())
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

  private fun makeSkillsPayment(
    returnUrl: String,
    productToken: String,
    encryptedCardNumber: String?,
    encryptedExpiryMonth: String?,
    encryptedExpiryYear: String?,
    encryptedSecurityCode: String
  ): Single<PaymentModel> {
    return getAndSignCurrentWalletAddress()
      .flatMap { address ->
        skillsPaymentRepository.makeSkillsPayment(
          returnUrl,
          address.address,
          address.signedAddress,
          productToken,
          encryptedCardNumber,
          encryptedExpiryMonth,
          encryptedExpiryYear,
          encryptedSecurityCode
        )
      }
  }


  @StringRes
  private fun mapAdyenErrorCode(errorCode: Int): Int {
    return when (errorCode) {
      AdyenErrorCodeMapper.DECLINED, AdyenErrorCodeMapper.BLOCKED_CARD, AdyenErrorCodeMapper.TRANSACTION_NOT_PERMITTED, AdyenErrorCodeMapper.REVOCATION_OF_AUTH, AdyenErrorCodeMapper.DECLINED_NON_GENERIC, AdyenErrorCodeMapper.ISSUER_SUSPECTED_FRAUD -> R.string.purchase_card_error_general_2
      AdyenErrorCodeMapper.REFERRAL, AdyenErrorCodeMapper.ACQUIRER_ERROR, AdyenErrorCodeMapper.ISSUER_UNAVAILABLE -> R.string.purchase_card_error_general_1
      AdyenErrorCodeMapper.EXPIRED_CARD -> R.string.purchase_card_error_expired
      AdyenErrorCodeMapper.INVALID_AMOUNT, AdyenErrorCodeMapper.NOT_ENOUGH_BALANCE, AdyenErrorCodeMapper.RESTRICTED_CARD -> R.string.purchase_card_error_no_funds
      AdyenErrorCodeMapper.INVALID_CARD_NUMBER -> R.string.purchase_card_error_invalid_details
      AdyenErrorCodeMapper.NOT_SUPPORTED -> R.string.purchase_card_error_not_supported
      AdyenErrorCodeMapper.INCORRECT_ONLINE_PIN, AdyenErrorCodeMapper.PIN_TRIES_EXCEEDED, AdyenErrorCodeMapper.NOT_3D_AUTHENTICATED -> R.string.purchase_card_error_security
      FRAUD, AdyenErrorCodeMapper.CANCELLED_DUE_TO_FRAUD -> R.string.purchase_error_fraud_code_20
      else -> R.string.purchase_card_error_title
    }
  }

  private fun mapError(errorType: ErrorType?): Int {
    return when (errorType) {
      ErrorType.BLOCKED -> R.string.purchase_error_wallet_block_code_403
      ErrorType.SUB_ALREADY_OWNED -> R.string.subscriptions_error_already_subscribed
      ErrorType.CONFLICT -> R.string.unknown_error //TODO should we have a different message for this
      else -> R.string.unknown_error
    }
  }

  companion object {

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val HTTP_FRAUD_CODE = 403
    private const val UID = "UID"
    private const val PAYMENT_DATA = "payment_data"
    private const val CHALLENGE_CANCELED = "Challenge canceled."
    private val TAG = _AdyenPaymentLogic::class.java.name
    private const val NOT_ALLOWED_CODE = "NotAllowed"
    private const val FORBIDDEN_CODE = "Authorization.Forbidden"
    private const val FIELDS_MISSING_CODE = "Body.Fields.Missing"
    private const val ADYEN_V2_ERROR = "AdyenV2.Error"
    private const val CONFLICT_HTTP_CODE = 409

    private const val MAX_NUMBER_OF_TRIES = 5
    private const val REQUEST_INTERVAL_IN_SECONDS: Long = 2
    private const val WALLET_VERIFIED = "wallet_verified_cc_"

    private const val TRANSACTION_HASH = "transaction_hash"
  }
}
