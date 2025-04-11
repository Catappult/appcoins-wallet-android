package com.asfoundation.wallet.billing.adyen

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.model.ModelObject
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.ALREADY_PROCESSED
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.CARD_SECURITY_VALIDATION
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.CURRENCY_NOT_SUPPORTED
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.CVC_LENGTH
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.CVC_REQUIRED
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.INVALID_CARD
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.INVALID_COUNTRY_CODE
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.OUTDATED_CARD
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.PAYMENT_ERROR
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.PAYMENT_NOT_SUPPORTED_ON_COUNTRY
import com.appcoins.wallet.billing.ErrorInfo.ErrorType.TRANSACTION_AMOUNT_EXCEEDED
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.REDIRECT
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2CHALLENGE
import com.appcoins.wallet.billing.adyen.AdyenResponseMapper.Companion.THREEDS2FINGERPRINT
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.sharedpreferences.CardPaymentDataSource
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.enums.PaymentStateEnum
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.asfoundation.wallet.manage_cards.usecases.GetPaymentInfoNewCardModelUseCase
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.topup.usecases.GetPaymentInfoFilterByCardModelUseCase
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.Navigator
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AdyenPaymentViewModel @Inject constructor(
  private val disposables: CompositeDisposable,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics,
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  private val adyenErrorCodeMapper: AdyenErrorCodeMapper,
  private val servicesErrorCodeMapper: ServicesErrorCodeMapper,
  private val formatter: CurrencyFormatUtils,
  private val logger: Logger,
  private val getStoredCardsUseCase: GetStoredCardsUseCase,
  private val cardPaymentDataSource: CardPaymentDataSource,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  private val getPaymentInfoNewCardModelUseCase: GetPaymentInfoNewCardModelUseCase,
  private val getPaymentInfoFilterByCardModelUseCase: GetPaymentInfoFilterByCardModelUseCase,
  rxSchedulers: RxSchedulers,
) : ViewModel() {

  val networkScheduler = rxSchedulers.io
  val viewScheduler = rxSchedulers.main
  private lateinit var paymentData: PaymentData
  private var waitingResult = false
  private var cachedUid = ""
  private var cachedPaymentData: String? = null
  private var action3ds: String? = null
  var isStored = false
  var askCVC = true
  var cardsList: List<StoredCard> = listOf()
  var storedCardID: String? = null
  var priceAmount: BigDecimal? = null
  var priceCurrency: String? = null
  private val _singleEventState = Channel<SingleEventState>(Channel.BUFFERED)
  val singleEventState = _singleEventState.receiveAsFlow()
  lateinit var paymentStateEnum: PaymentStateEnum
  var cancelPaypalLaunch = false

  sealed class SingleEventState {
    object setup3DSComponent : SingleEventState()
    object setupRedirectComponent : SingleEventState()
    object showLoading : SingleEventState()
    object showGenericError : SingleEventState()
    object hideLoadingAndShowView : SingleEventState()
    object showNetworkError : SingleEventState()
    data class finishCardConfiguration(
      val paymentInfoModel: PaymentInfoModel,
      val forget: Boolean
    ) : SingleEventState()

    object restartFragment : SingleEventState()
    data class showProductPrice(
      val amount: String,
      val currencyCode: String
    ) : SingleEventState()

    object lockRotation : SingleEventState()
    object showLoadingMakingPayment : SingleEventState()
    object hideKeyboard : SingleEventState()
    object showCvvError : SingleEventState()
    object showMoreMethods : SingleEventState()
    data class showSuccess(val renewal: Date?) : SingleEventState()
    data class showSpecificError(val stringRes: Int, val backToCard: Boolean = false) :
      SingleEventState()

    data class showVerificationError(val isWalletVerified: Boolean) : SingleEventState()
    data class showVerification(
      val isWalletVerified: Boolean,
      val paymentType: String
    ) : SingleEventState()

    data class handleCreditCardNeedCVC(val needCVC: Boolean) : SingleEventState()
    data class close(val bundle: Bundle?) : SingleEventState()
    data class submitUriResult(val uri: Uri) : SingleEventState()
    object showBackToCard : SingleEventState()
    data class handle3DSAction(val action: Action) : SingleEventState()
    object showInvalidCardError : SingleEventState()
    object showSecurityValidationError : SingleEventState()
    object showOutdatedCardError : SingleEventState()
    object showAlreadyProcessedError : SingleEventState()
    object showPaymentError : SingleEventState()
    object showCvcRequired : SingleEventState()
  }

  fun sendSingleEvent(state: SingleEventState) {
    viewModelScope.launch {
      _singleEventState.send(state)
    }
  }

  data class PaymentData(
    val returnUrl: String,
    val origin: String?,
    val transactionBuilder: TransactionBuilder,
    val paymentType: String,
    val amount: BigDecimal,
    val currency: String,
    val skills: Boolean,
    val isPreSelected: Boolean,
    val gamificationLevel: Int,
    val navigator: Navigator,
    val iabView: IabView,
  )

  fun initialize(
    savedInstanceState: Bundle?,
    paymentData: PaymentData,
    adyenSupportIconClicks: Observable<Any>,
    adyenSupportLogoClicks: Observable<Any>,
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>,
    verificationClicks: Observable<Boolean>,
    paymentDetails: Observable<AdyenComponentResponseModel>,
    onAdyen3DSError: Observable<String>,
    errorDismisses: Observable<Any>,
    backEvent: Observable<Any>,
    morePaymentMethodsClicks: Observable<Any>,
    adyenErrorBackClicks: Observable<Any>,
    adyenErrorBackToCardClicks: Observable<Any>,
    adyenErrorCancelClicks: Observable<Any>,
    paymentStateEnumArgs: String?
  ) {
    this.paymentData = paymentData
    paymentStateEnum = if (paymentStateEnumArgs == PaymentStateEnum.PAYMENT_WITH_NEW_CARD.state) {
      PaymentStateEnum.PAYMENT_WITH_NEW_CARD
    } else {
      PaymentStateEnum.UNDEFINED
    }
    retrieveSavedInstace(savedInstanceState)
    sendSingleEvent(SingleEventState.setup3DSComponent)
    sendSingleEvent(SingleEventState.setupRedirectComponent)
    if (!waitingResult) getCardIdSharedPreferences(
      retrievePaymentData,
      buyButtonClicked
    )
    handleBack(backEvent)
    handleErrorDismissEvent(errorDismisses)
    handleRedirectResponse()
    handlePaymentDetails(paymentDetails)
    handleAdyenErrorBack(adyenErrorBackClicks)
    handleAdyenErrorBackToCard(
      retrievePaymentData,
      buyButtonClicked,
      adyenErrorBackToCardClicks
    )
    handleAdyenErrorCancel(adyenErrorCancelClicks)
    handleSupportClicks(adyenSupportIconClicks, adyenSupportLogoClicks)
    handle3DSErrors(onAdyen3DSError)
    handleVerificationClick(verificationClicks)
    handleCreditCardNeedCVC()
    handleMorePaymentsClick(morePaymentMethodsClicks)
  }

  private fun handleSupportClicks(
    adyenSupportIconClicks: Observable<Any>,
    adyenSupportLogoClicks: Observable<Any>
  ) {
    disposables.add(
      Observable.merge(
        adyenSupportIconClicks,
        adyenSupportLogoClicks
      )
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          adyenPaymentInteractor.showSupport(cachedUid)
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }


  private fun askCardDetails() {
    disposables.add(
      adyenPaymentInteractor.loadPaymentInfo(
        mapPaymentToService(paymentData.paymentType),
        paymentData.amount.toString(),
        paymentData.currency
      )
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.error.hasError) {
            if (it.error.isNetworkError) sendSingleEvent(SingleEventState.showNetworkError)
            else sendSingleEvent(SingleEventState.showGenericError)
          } else {
            sendSingleEvent(SingleEventState.restartFragment)
          }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun setPaymentStateEnum(
    state: PaymentStateEnum,
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>
  ) {
    if (state == PaymentStateEnum.UNDEFINED) {
      when (paymentData.paymentType) {
        PaymentType.CARD.name -> {
          paymentStateEnum =
            if (!storedCardID.isNullOrEmpty()) PaymentStateEnum.PAYMENT_WITH_STORED_CARD_ID else PaymentStateEnum.PAYMENT_WITH_NEW_CARD
        }

        PaymentType.PAYPAL.name -> paymentStateEnum = PaymentStateEnum.PAYMENT_WITH_PAYPAL
      }
    } else {
      paymentStateEnum = state
    }
    loadPaymentMethodInfo(retrievePaymentData, buyButtonClicked)
  }

  private fun loadPaymentMethodInfo(
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>
  ) {
    sendSingleEvent(SingleEventState.showLoading)
    when (paymentStateEnum) {
      PaymentStateEnum.PAYMENT_WITH_PAYPAL -> {
        loadPaymentMethodPaypal()
      }

      PaymentStateEnum.PAYMENT_WITH_NEW_CARD -> loadPaymentMethodWithNewCard(
        retrievePaymentData,
        buyButtonClicked
      )

      PaymentStateEnum.PAYMENT_WITH_STORED_CARD_ID ->
        loadPaymentMethodWithStoredCard(
          retrievePaymentData,
          buyButtonClicked
        )

      PaymentStateEnum.UNDEFINED -> {}
    }

  }

  private fun loadPaymentMethodPaypal() {
    disposables.add(
      adyenPaymentInteractor.loadPaymentInfo(
        mapPaymentToService(paymentData.paymentType),
        paymentData.amount.toString(),
        paymentData.currency
      )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.error.hasError) {
            sendPaymentErrorEvent(it.error.errorInfo?.httpCode, it.error.errorInfo?.text)
            sendSingleEvent(SingleEventState.hideLoadingAndShowView)
            handleErrors(it.error)
          } else {
            val amount = formatter.formatPaymentCurrency(it.priceAmount, WalletCurrency.FIAT)
            sendSingleEvent(SingleEventState.showProductPrice(amount, it.priceCurrency))
            launchPaymentAdyen(it.paymentMethod!!, it.priceAmount, it.priceCurrency)
          }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun loadPaymentMethodWithNewCard(
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>
  ) {
    disposables.add(
      getPaymentInfoNewCardModelUseCase(paymentData.amount.toString(), paymentData.currency)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.error.hasError) {
            sendPaymentErrorEvent(it.error.errorInfo?.httpCode, it.error.errorInfo?.text)
            sendSingleEvent(SingleEventState.hideLoadingAndShowView)
            handleErrors(it.error)
          } else {
            val amount = formatter.formatPaymentCurrency(it.priceAmount, WalletCurrency.FIAT)
            sendSingleEvent(SingleEventState.showProductPrice(amount, it.priceCurrency))
            priceAmount = it.priceAmount
            priceCurrency = it.priceCurrency
            sendSingleEvent(SingleEventState.hideLoadingAndShowView)
            sendPaymentMethodDetailsEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
            sendSingleEvent(SingleEventState.finishCardConfiguration(it, false))
            handleBuyClick(
              it.priceAmount,
              it.priceCurrency,
              retrievePaymentData,
              buyButtonClicked
            )
            paymentAnalytics.stopTimingForTotalEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)

          }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun loadPaymentMethodWithStoredCard(
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>
  ) {
    disposables.add(
      getPaymentInfoFilterByCardModelUseCase(
        paymentData.amount.toString(),
        paymentData.currency,
        storedCardID!!
      )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.error.hasError) {
            sendPaymentErrorEvent(it.error.errorInfo?.httpCode, it.error.errorInfo?.text)
            sendSingleEvent(SingleEventState.hideLoadingAndShowView)
            handleErrors(it.error)
          } else {
            val amount = formatter.formatPaymentCurrency(it.priceAmount, WalletCurrency.FIAT)
            sendSingleEvent(SingleEventState.showProductPrice(amount, it.priceCurrency))
            priceAmount = it.priceAmount
            priceCurrency = it.priceCurrency
            sendSingleEvent(SingleEventState.hideLoadingAndShowView)
            sendPaymentMethodDetailsEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
            sendSingleEvent(SingleEventState.finishCardConfiguration(it, false))
            handleBuyClick(
              it.priceAmount,
              it.priceCurrency,
              retrievePaymentData,
              buyButtonClicked
            )
            paymentAnalytics.stopTimingForTotalEvent(PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
          }
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun getStoredCardsList(
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>
  ) {
    disposables.add(
      getStoredCardsUseCase().subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          cardsList = it.map {
            StoredCard(
              cardLastNumbers = it.lastFour ?: "****",
              cardIcon = PaymentBrands.getPayment(it.brand).brandFlag,
              recurringReference = it.id,
              !storedCardID.isNullOrEmpty() && it.id == storedCardID
            )
          }
          if (!cardsList.isNullOrEmpty() && storedCardID.isNullOrEmpty()) {
            cardsList.first().let {
              it.isSelectedCard = true
              storedCardID = it.recurringReference.toString()
              setCardIdSharedPreferences(it.recurringReference.toString())
            }
          }
          setPaymentStateEnum(
            paymentStateEnum, retrievePaymentData,
            buyButtonClicked
          )
        }.subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun getCardIdSharedPreferences(
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>,
  ) {
    disposables.add(
      getCurrentWalletUseCase()
        .subscribeOn(networkScheduler)
        .subscribe(
          {
            storedCardID = cardPaymentDataSource.getPreferredCardId(it.address)
            getStoredCardsList(retrievePaymentData, buyButtonClicked)
          },
          { }
        )
    )
  }

  fun setCardIdSharedPreferences(recurringReference: String) {
    disposables.add(
      getCurrentWalletUseCase()
        .subscribeOn(networkScheduler)
        .subscribe(
          { cardPaymentDataSource.setPreferredCardId(recurringReference, it.address) },
          { }
        )
    )
  }

  private fun launchPaymentAdyen(
    paymentMethodInfo: ModelObject,
    priceAmount: BigDecimal,
    priceCurrency: String
  ) {
    disposables.add(
      adyenPaymentInteractor.makePayment(
        adyenPaymentMethod = paymentMethodInfo,
        shouldStoreMethod = false,
        hasCvc = false,
        supportedShopperInteraction = emptyList(),
        returnUrl = paymentData.returnUrl,
        value = priceAmount.toString(),
        currency = priceCurrency,
        reference = paymentData.transactionBuilder.orderReference,
        paymentType = mapPaymentToService(paymentData.paymentType).transactionType,
        origin = paymentData.origin,
        packageName = paymentData.transactionBuilder.domain,
        metadata = paymentData.transactionBuilder.payload,
        sku = paymentData.transactionBuilder.skuId,
        callbackUrl = paymentData.transactionBuilder.callbackUrl,
        transactionType = paymentData.transactionBuilder.type,
        referrerUrl = paymentData.transactionBuilder.referrerUrl,
        guestWalletId = paymentData.transactionBuilder.guestWalletId,
        externalBuyerReference = paymentData.transactionBuilder.externalBuyerReference,
        isFreeTrial = paymentData.transactionBuilder.isFreeTrial,
      )
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .filter { !waitingResult }
        .doOnSuccess {
          sendSingleEvent(SingleEventState.hideLoadingAndShowView)
          cachedUid = it.uid
          handlePaymentModel(it)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun handlePaymentModel(paymentModel: PaymentModel) {
    if (paymentModel.error.hasError) {
      handleErrors(paymentModel.error, paymentModel.refusalCode)
    } else {
      sendSingleEvent(SingleEventState.showLoading)
      sendSingleEvent(SingleEventState.lockRotation)
      sendPaymentMethodDetailsEvent(mapPaymentToAnalytics(paymentData.paymentType))
      paymentAnalytics.stopTimingForTotalEvent(mapPaymentToAnalytics(paymentData.paymentType))
      paymentAnalytics.startTimingForPurchaseEvent()
      handleAdyenAction(paymentModel)
    }
  }

  data class BuyClickData(
    val shouldStoreCard: Boolean,
  )

  private fun handleBuyClick(
    priceAmount: BigDecimal,
    priceCurrency: String,
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>,
  ) {
    disposables.add(
      buyButtonClicked
        .flatMapSingle { buyClickData ->
          paymentAnalytics.startTimingForPurchaseEvent()
          retrievePaymentData?.firstOrError()?.map {
            Pair(
              it,
              buyClickData
            )
          } ?: Single.error(Throwable("Payment data not found"))
        }
        .observeOn(viewScheduler)
        .doOnNext {
          sendSingleEvent(SingleEventState.showLoadingMakingPayment)
          sendSingleEvent(SingleEventState.hideKeyboard)
          sendSingleEvent(SingleEventState.lockRotation)
        }
        .observeOn(networkScheduler)
        .flatMapSingle { pair ->
          val adyenCard = pair.first
          val shouldStore = pair.second.shouldStoreCard
          handleBuyAnalytics(paymentData.transactionBuilder)
          adyenPaymentInteractor.makePayment(
            adyenPaymentMethod = adyenCard.cardPaymentMethod,
            shouldStoreMethod = shouldStore,
            hasCvc = adyenCard.hasCvc,
            supportedShopperInteraction = adyenCard.supportedShopperInteractions,
            returnUrl = paymentData.returnUrl,
            value = priceAmount.toString(),
            currency = priceCurrency,
            reference = paymentData.transactionBuilder.orderReference,
            paymentType = mapPaymentToService(paymentData.paymentType).transactionType,
            origin = paymentData.origin,
            packageName = paymentData.transactionBuilder.domain,
            metadata = paymentData.transactionBuilder.payload,
            sku = paymentData.transactionBuilder.skuId,
            callbackUrl = paymentData.transactionBuilder.callbackUrl,
            transactionType = paymentData.transactionBuilder.type,
            referrerUrl = paymentData.transactionBuilder.referrerUrl,
            guestWalletId = paymentData.transactionBuilder.guestWalletId,
            externalBuyerReference = paymentData.transactionBuilder.externalBuyerReference,
            isFreeTrial = paymentData.transactionBuilder.isFreeTrial,
          )

        }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          cachedUid = it.uid
          handlePaymentResult(it)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun handlePaymentResult(
    paymentModel: PaymentModel
  ): Completable = when {
    paymentModel.resultCode.equals("AUTHORISED", true) -> {
      adyenPaymentInteractor.getAuthorisedTransaction(paymentModel.uid)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .flatMapCompletable {
          when {
            it.status == PaymentModel.Status.COMPLETED -> {
              sendPaymentSuccessEvent(it.uid)
              createBundle(it.hash, it.orderReference, it.purchaseUid)
                .doOnSuccess {
                  sendPaymentEvent()
                  sendRevenueEvent()
                }
                .subscribeOn(networkScheduler)
                .observeOn(viewScheduler)
                .flatMapCompletable { bundle ->
                  handleSuccessTransaction(
                    bundle
                  )
                }
            }

            isPaymentFailed(it.status) -> {
              if (paymentModel.status == PaymentModel.Status.FAILED && paymentData.paymentType == PaymentType.PAYPAL.name) {
                retrieveFailedReason(paymentModel.uid)
              } else {
                Completable.fromAction {
                  var errorDetails = buildRefusalReason(it.status, it.error.errorInfo?.text)
                  if (errorDetails.isBlank()) {
                    errorDetails = getWebViewResultCode()
                  }
                  sendPaymentErrorEvent(
                    it.error.errorInfo?.httpCode,
                    errorDetails
                  )
                  handleErrors(it.error, paymentModel.refusalCode)
                }
                  .subscribeOn(viewScheduler)
              }
            }

            else -> {
              var errorDetails = it.status.toString() + it.error.errorInfo?.text
              if (errorDetails.isBlank()) {
                errorDetails = getWebViewResultCode()
              }
              sendPaymentErrorEvent(
                it.error.errorInfo?.httpCode,
                errorDetails
              )
              Completable.fromAction { handleErrors(it.error, it.refusalCode) }
            }
          }
        }
    }

    paymentModel.status == PaymentModel.Status.PENDING_USER_PAYMENT && paymentModel.action != null -> {
      Completable.fromAction {
        sendSingleEvent(SingleEventState.showLoading)
        sendSingleEvent(SingleEventState.lockRotation)
        handleAdyenAction(paymentModel)
      }
    }

    paymentModel.refusalReason != null -> Completable.fromAction {
      var riskRules: String? = null
      paymentModel.refusalCode?.let { code ->
        when (code) {
          AdyenErrorCodeMapper.CVC_DECLINED -> sendSingleEvent(SingleEventState.showCvvError)
          AdyenErrorCodeMapper.FRAUD -> {
            handleFraudFlow(adyenErrorCodeMapper.map(code), paymentModel.fraudResultIds)
            riskRules = paymentModel.fraudResultIds.sorted()
              .joinToString(separator = "-")
          }

          else -> handleErrors(paymentModel.error, code)
        }
      }
      var errorDetails = paymentModel.refusalReason
      if (errorDetails.isNullOrBlank()) {
        errorDetails = getWebViewResultCode()
      }
      sendPaymentErrorEvent(paymentModel.refusalCode, errorDetails, riskRules)
    }

    paymentModel.error.hasError -> Completable.fromAction {
      var errorDetails = paymentModel.error.errorInfo?.text
      if (errorDetails.isNullOrBlank()) {
        errorDetails = getWebViewResultCode()
      }
      sendPaymentErrorEvent(
        paymentModel.error.errorInfo?.httpCode,
        errorDetails
      )
      handleErrors(paymentModel.error, paymentModel.refusalCode)
    }

    paymentModel.status == PaymentModel.Status.FAILED && paymentData.paymentType == PaymentType.PAYPAL.name -> {
      retrieveFailedReason(paymentModel.uid)
    }

    paymentModel.status == PaymentModel.Status.CANCELED -> Completable.fromAction {
      sendSingleEvent(
        SingleEventState.showMoreMethods
      )
    }

    else -> Completable.fromAction {
      var errorDetails = "${paymentModel.status} ${paymentModel.error.errorInfo?.text}"
      if (errorDetails.isBlank()) {
        errorDetails = getWebViewResultCode()
      }
      sendPaymentErrorEvent(
        paymentModel.error.errorInfo?.httpCode,
        errorDetails
      )
      sendSingleEvent(SingleEventState.showGenericError)
    }
  }

  private fun getWebViewResultCode(): String {
    return "webView Result: ${paymentData.iabView.webViewResultCode}"
  }

  private fun handleSuccessTransaction(
    purchaseBundleModel: PurchaseBundleModel
  ): Completable =
    Completable.fromAction { sendSingleEvent(SingleEventState.showSuccess(purchaseBundleModel.renewal)) }
      .andThen(Completable.timer(SUCCESS_DURATION, TimeUnit.MILLISECONDS, viewScheduler))
      .andThen(Completable.fromAction { paymentData.navigator.popView(purchaseBundleModel.bundle) })

  private fun retrieveFailedReason(uid: String): Completable =
    adyenPaymentInteractor.getFailedTransactionReason(uid)
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .flatMapCompletable {
        Completable.fromAction {
          sendPaymentErrorEvent(it.errorCode, it.errorMessage ?: "")
          if (it.errorCode != null) {
            sendSingleEvent(SingleEventState.showSpecificError(adyenErrorCodeMapper.map(it.errorCode!!)))
          } else {
            sendSingleEvent(SingleEventState.showGenericError)
          }
        }
      }

  @Suppress("UNUSED_PARAMETER")
  private fun handleFraudFlow(@StringRes error: Int, fraudCheckIds: List<Int>) {
    val verificationType = if (paymentData.paymentType == PaymentType.CARD.name) {
      VerificationType.CREDIT_CARD
    } else {
      VerificationType.PAYPAL
    }
    disposables.add(
      adyenPaymentInteractor.isWalletVerified(verificationType)
        .observeOn(viewScheduler)
        .doOnSuccess { verified ->
          sendSingleEvent(SingleEventState.showVerificationError(verified))
        }
        .subscribe({}, { sendSingleEvent(SingleEventState.showSpecificError(error)) })
    )
  }

  private fun handleVerificationClick(verificationClicks: Observable<Boolean>) {
    disposables.add(
      verificationClicks
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { isWalletVerified ->
          sendSingleEvent(
            SingleEventState.showVerification(
              isWalletVerified,
              paymentData.paymentType
            )
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun buildRefusalReason(status: PaymentModel.Status, message: String?): String =
    message?.let { "$status : $it" } ?: status.toString()

  private fun isPaymentFailed(status: PaymentModel.Status): Boolean =
    status == PaymentModel.Status.FAILED || status == PaymentModel.Status.CANCELED || status == PaymentModel.Status.INVALID_TRANSACTION || status == PaymentModel.Status.FRAUD

  private fun handlePaymentDetails(
    paymentDetails: Observable<AdyenComponentResponseModel>
  ) {
    disposables.add(
      paymentDetails
        .throttleLast(2, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { sendSingleEvent(SingleEventState.lockRotation) }
        .observeOn(networkScheduler)
        .flatMapSingle {
          adyenPaymentInteractor.submitRedirect(
            uid = cachedUid,
            details = convertToJson(it.details!!),
            paymentData = it.paymentData ?: cachedPaymentData
          )
        }
        .observeOn(viewScheduler)
        .flatMapCompletable {
          cachedUid = it.uid
          handlePaymentResult(it)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
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

  private fun handle3DSErrors(onAdyen3DSError: Observable<String>) {
    disposables.add(
      onAdyen3DSError
        .observeOn(viewScheduler)
        .doOnNext {
          if (it == CHALLENGE_CANCELED) {
            paymentAnalytics.send3dsCancel()
            sendSingleEvent(SingleEventState.showMoreMethods)
          } else {
            paymentAnalytics.send3dsError(it)
            logger.log(TAG, "error:$it \n last 3ds action: ${action3ds ?: ""}")
            sendSingleEvent(SingleEventState.showGenericError)
          }
        }
        .subscribe({}, { it.printStackTrace() })
    )
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
    disposables.add(
      Single.just(paymentData.transactionBuilder)
        .observeOn(networkScheduler)
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
    )
  }

  private fun handleErrorDismissEvent(errorDismisses: Observable<Any>) {
    disposables.add(
      errorDismisses
        .observeOn(viewScheduler)
        .doOnNext { paymentData.navigator.popViewWithError() }
        .subscribe({}, { paymentData.navigator.popViewWithError() })
    )
  }

  private fun handleCreditCardNeedCVC() {
    disposables.add(
      adyenPaymentInteractor.getCreditCardNeedCVC()
        .observeOn(viewScheduler)
        .doOnSuccess {
          sendSingleEvent(SingleEventState.handleCreditCardNeedCVC(it.needAskCvc))
        }
        .doOnError {
          sendSingleEvent(SingleEventState.handleCreditCardNeedCVC(true))
        }
        .subscribe()
    )
  }

  private fun handleBack(backEvent: Observable<Any>) {
    disposables.add(
      backEvent
        .observeOn(networkScheduler)
        .doOnNext { handlePaymentMethodAnalytics(paymentData.transactionBuilder) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handlePaymentMethodAnalytics(transaction: TransactionBuilder) {
    if (paymentData.isPreSelected) {
      analytics.sendPreSelectedPaymentMethodEvent(
        paymentData.transactionBuilder.domain,
        transaction.skuId,
        transaction.amount().toString(),
        mapPaymentToService(paymentData.paymentType).transactionType,
        transaction.type,
        BillingAnalytics.ACTION_CANCEL
      )
      sendSingleEvent(SingleEventState.close(adyenPaymentInteractor.mapCancellation()))
    } else {
      analytics.sendPaymentConfirmationEvent(
        paymentData.transactionBuilder.domain,
        transaction.skuId,
        transaction.amount().toString(),
        mapPaymentToService(paymentData.paymentType).transactionType,
        transaction.type,
        BillingAnalytics.ACTION_BACK
      )
      sendSingleEvent(SingleEventState.showMoreMethods)
    }
  }

  private fun handleMorePaymentsClick(morePaymentMethodsClicks: Observable<Any>) {
    disposables.add(
      morePaymentMethodsClicks
        .observeOn(networkScheduler)
        .doOnNext { handleMorePaymentsAnalytics(paymentData.transactionBuilder) }
        .observeOn(viewScheduler)
        .doOnNext { showMoreMethods() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleMorePaymentsAnalytics(transaction: TransactionBuilder): Unit =
    analytics.sendPreSelectedPaymentMethodEvent(
      paymentData.transactionBuilder.domain,
      transaction.skuId,
      transaction.amount().toString(),
      mapPaymentToService(paymentData.paymentType).transactionType,
      transaction.type,
      "other_payments"
    )

  private fun handleRedirectResponse() {
    disposables.add(
      paymentData.navigator.uriResults()
        .observeOn(viewScheduler)
        .doOnNext { sendSingleEvent(SingleEventState.submitUriResult(it)) }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun showMoreMethods() {
    adyenPaymentInteractor.removePreSelectedPaymentMethod()
    sendSingleEvent(SingleEventState.showMoreMethods)
  }

  private fun sendPaymentEvent() {
    disposables.add(
      Single.just(paymentData.transactionBuilder)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe { transactionBuilder ->
          stopTimingForPurchaseEvent(true)
          analytics.sendPaymentEvent(
            transactionBuilder.domain,
            transactionBuilder.skuId,
            transactionBuilder.amount().toString(),
            mapPaymentToAnalytics(paymentData.paymentType),
            transactionBuilder.type
          )
        })
  }

  private fun sendRevenueEvent() {
    disposables.add(
      Single.just(paymentData.transactionBuilder)
        .observeOn(networkScheduler)
        .doOnSuccess { transactionBuilder ->
          analytics.sendRevenueEvent(
            adyenPaymentInteractor.convertToFiat(
              transactionBuilder.amount().toDouble(),
              BillingAnalytics.EVENT_REVENUE_CURRENCY
            )
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .blockingGet()
              .amount
              .setScale(2, BigDecimal.ROUND_UP)
              .toString()
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentSuccessEvent(txId: String) {
    disposables.add(
      Single.just(paymentData.transactionBuilder)
        .observeOn(networkScheduler)
        .doOnSuccess { transaction ->
          val mappedPaymentType = mapPaymentToAnalytics(paymentData.paymentType)
          analytics.sendPaymentSuccessEvent(
            packageName = paymentData.transactionBuilder.domain,
            skuDetails = transaction.skuId,
            value = transaction.amount().toString(),
            purchaseDetails = mapPaymentToAnalytics(paymentData.paymentType),
            transactionType = transaction.type,
            txId = txId,
            valueUsd = transaction.amountUsd.toString(),
            isStoredCard =
              if (mappedPaymentType == PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
                isStored
              else null,
            wasCvcRequired =
              if (mappedPaymentType == PaymentMethodsAnalytics.PAYMENT_METHOD_CC)
                askCVC
              else null,
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun sendPaymentErrorEvent(
    refusalCode: Int?,
    refusalReason: String?,
    riskRules: String? = null
  ) {
    disposables.add(
      Single.just(paymentData.transactionBuilder)
        .observeOn(networkScheduler)
        .doOnSuccess { transaction ->
          stopTimingForPurchaseEvent(false)
          analytics.sendPaymentErrorWithDetailsAndRiskEvent(
            paymentData.transactionBuilder.domain,
            transaction.skuId,
            transaction.amount().toString(),
            mapPaymentToAnalytics(paymentData.paymentType),
            transaction.type,
            refusalCode.toString(),
            refusalReason,
            riskRules
          )
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun mapPaymentToAnalytics(paymentType: String): String =
    if (paymentType == PaymentType.CARD.name) {
      PaymentMethodsAnalytics.PAYMENT_METHOD_CC
    } else {
      PaymentMethodsAnalytics.PAYMENT_METHOD_PP
    }

  private fun mapPaymentToService(paymentType: String): AdyenPaymentRepository.Methods =
    when (paymentType) {
      PaymentType.CARD.name -> {
        AdyenPaymentRepository.Methods.CREDIT_CARD
      }

      else -> {
        AdyenPaymentRepository.Methods.PAYPAL
      }
    }

  private fun createBundle(
    hash: String?,
    orderReference: String?,
    purchaseUid: String?
  ): Single<PurchaseBundleModel> =
    adyenPaymentInteractor
      .getCompletePurchaseBundle(
        paymentData.transactionBuilder.type,
        paymentData.transactionBuilder.domain,
        paymentData.transactionBuilder.skuId,
        purchaseUid,
        orderReference,
        hash,
        networkScheduler
      )
      .map { mapPaymentMethodId(it) }

  private fun mapPaymentMethodId(purchaseBundleModel: PurchaseBundleModel): PurchaseBundleModel {
    val bundle = purchaseBundleModel.bundle
    if (paymentData.paymentType == PaymentType.CARD.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id
      )
    } else if (paymentData.paymentType == PaymentType.PAYPAL.name) {
      bundle.putString(
        InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY,
        PaymentMethodsView.PaymentMethodId.PAYPAL.id
      )
    }
    return PurchaseBundleModel(bundle, purchaseBundleModel.renewal)
  }

  private fun handleBuyAnalytics(transactionBuilder: TransactionBuilder) =
    if (paymentData.isPreSelected) {
      analytics.sendPreSelectedPaymentMethodEvent(
        transactionBuilder.domain,
        transactionBuilder.skuId,
        transactionBuilder.amount().toString(),
        mapPaymentToService(paymentData.paymentType).transactionType,
        transactionBuilder.type,
        BillingAnalytics.ACTION_BUY
      )
    } else {
      analytics.sendPaymentConfirmationEvent(
        transactionBuilder.domain,
        transactionBuilder.skuId,
        transactionBuilder.amount().toString(),
        mapPaymentToService(paymentData.paymentType).transactionType,
        transactionBuilder.type,
        BillingAnalytics.ACTION_BUY
      )
    }

  private fun handleAdyenErrorBack(adyenErrorBackClicks: Observable<Any>) {
    disposables.add(
      adyenErrorBackClicks
        .observeOn(viewScheduler)
        .doOnNext {
          adyenPaymentInteractor.removePreSelectedPaymentMethod()
          sendSingleEvent(SingleEventState.showMoreMethods)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun handleAdyenErrorBackToCard(
    retrievePaymentData: ReplaySubject<AdyenCardWrapper>?,
    buyButtonClicked: Observable<BuyClickData>,
    adyenErrorBackToCardClicks: Observable<Any>,
  ) {
    disposables.add(
      adyenErrorBackToCardClicks
        .observeOn(viewScheduler)
        .doOnNext {
          adyenPaymentInteractor.removePreSelectedPaymentMethod()
          if (priceAmount != null && priceCurrency != null) {
            handleBuyClick(
              priceAmount!!,
              priceCurrency!!,
              retrievePaymentData,
              buyButtonClicked
            )
          }
          sendSingleEvent(SingleEventState.showBackToCard)
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun handleAdyenErrorCancel(adyenErrorCancelClicks: Observable<Any>) {
    disposables.add(
      adyenErrorCancelClicks
        .observeOn(viewScheduler)
        .doOnNext {
          sendSingleEvent(SingleEventState.close(adyenPaymentInteractor.mapCancellation()))
        }
        .subscribe({}, {
          logger.log(TAG, it)
          sendSingleEvent(SingleEventState.showGenericError)
        })
    )
  }

  private fun handleAdyenAction(paymentModel: PaymentModel) {
    if (paymentModel.action != null) {
      when (val type = paymentModel.action?.type) {
        REDIRECT -> {
          action3ds = type
          paymentAnalytics.send3dsStart(action3ds)
          cachedPaymentData = paymentModel.paymentData
          cachedUid = paymentModel.uid
          if (!cancelPaypalLaunch)
            paymentData.navigator.navigateToUriForResult(paymentModel.redirectUrl)
          waitingResult = true
        }

        THREEDS2, THREEDS2FINGERPRINT, THREEDS2CHALLENGE -> {
          action3ds = type
          paymentAnalytics.send3dsStart(action3ds)
          cachedUid = paymentModel.uid
          sendSingleEvent(SingleEventState.handle3DSAction(paymentModel.action!!))
          waitingResult = true
        }

        else -> {
          logger.log(TAG, "Unknown adyen action: $type")
          sendSingleEvent(SingleEventState.showGenericError)
        }
      }
    }
  }

  fun stop() = disposables.clear()

  private fun handleErrors(error: Error, code: Int? = null) {
    when {
      error.isNetworkError -> sendSingleEvent(SingleEventState.showNetworkError)
      error.errorInfo?.errorType == INVALID_CARD -> sendSingleEvent(
        SingleEventState.showInvalidCardError
      )

      error.errorInfo?.errorType == CARD_SECURITY_VALIDATION -> sendSingleEvent(
        SingleEventState.showSecurityValidationError
      )

      error.errorInfo?.errorType == OUTDATED_CARD -> sendSingleEvent(
        SingleEventState.showOutdatedCardError
      )

      error.errorInfo?.errorType == ALREADY_PROCESSED -> sendSingleEvent(
        SingleEventState.showAlreadyProcessedError
      )

      error.errorInfo?.errorType == PAYMENT_ERROR -> sendSingleEvent(
        SingleEventState.showPaymentError
      )

      error.errorInfo?.errorType == INVALID_COUNTRY_CODE -> sendSingleEvent(
        SingleEventState.showSpecificError(
          R.string.unknown_error
        )
      )

      error.errorInfo?.errorType == PAYMENT_NOT_SUPPORTED_ON_COUNTRY -> sendSingleEvent(
        SingleEventState.showSpecificError(
          R.string.purchase_error_payment_rejected
        )
      )

      error.errorInfo?.errorType == CURRENCY_NOT_SUPPORTED -> sendSingleEvent(
        SingleEventState.showSpecificError(
          R.string.purchase_card_error_general_1
        )
      )

      error.errorInfo?.errorType == CVC_LENGTH -> sendSingleEvent(
        SingleEventState.showCvvError
      )

      error.errorInfo?.errorType == TRANSACTION_AMOUNT_EXCEEDED -> sendSingleEvent(
        SingleEventState.showSpecificError(
          R.string.purchase_card_error_no_funds
        )
      )

      error.errorInfo?.errorType == CVC_REQUIRED -> {
        adyenPaymentInteractor.setMandatoryCVC(true)
        askCardDetails()
      }

      error.errorInfo?.httpCode != null -> {
        val resId = servicesErrorCodeMapper.mapError(error.errorInfo?.errorType)
        if (error.errorInfo?.httpCode == HTTP_FRAUD_CODE) handleFraudFlow(resId, emptyList())
        else sendSingleEvent(SingleEventState.showSpecificError(resId))
      }

      else -> {
        val isBackToCardAction = adyenErrorCodeMapper.needsCardRepeat(code ?: 0)
        sendSingleEvent(
          SingleEventState.showSpecificError(
            adyenErrorCodeMapper.map(code ?: 0),
            isBackToCardAction
          )
        )
      }
    }
  }

  private fun stopTimingForPurchaseEvent(success: Boolean) {
    val paymentMethod = when (paymentData.paymentType) {
      PaymentType.PAYPAL.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_PP
      PaymentType.CARD.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_CC
      else -> return
    }
    paymentAnalytics.stopTimingForPurchaseEvent(paymentMethod, success, paymentData.isPreSelected)
  }

  companion object {
    private val TAG = AdyenPaymentViewModel::class.java.simpleName
    private const val WAITING_RESULT = "WAITING_RESULT"
    private const val HTTP_FRAUD_CODE = 403
    private const val UID = "UID"
    private const val PAYMENT_DATA = "payment_data"
    private const val CHALLENGE_CANCELED = "Challenge canceled."
    private const val SUCCESS_DURATION = 3000L
  }

}
