package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.billing.carrierbilling.*
import com.appcoins.wallet.billing.carrierbilling.ForbiddenError.ForbiddenType
import com.appcoins.wallet.billing.carrierbilling.request.CarrierTransactionBody
import com.appcoins.wallet.billing.carrierbilling.response.CarrierCreateTransactionResponse
import com.appcoins.wallet.billing.carrierbilling.response.CountryListResponse
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.appcoins.wallet.billing.util.isNoNetworkException
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.carrier_billing.CarrierBillingRepository
import com.asfoundation.wallet.billing.carrier_billing.CarrierErrorResponse
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import com.asfoundation.wallet.di.annotations.BrokerDefaultRetrofit
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.repository.SignDataStandardNormalizer
import com.asfoundation.wallet.repository.WalletNotFoundException
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.payments.carrier.TransactionDataDetails
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletAddresses
import com.asfoundation.wallet.ui.iab.payments.common.model.WalletStatus
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import org.kethereum.erc681.isEthereumURLString
import org.kethereum.erc681.parseERC681
import org.web3j.crypto.Keys
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Retrofit
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class _CarrierVerifyLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val data: CarrierVerifyData,
  @ApplicationContext val context: Context,
  private val api: CarrierBillingRepository.CarrierBillingApi,
  private val promoCodeRepository: PromoCodeRepository,
  private val accountKeystoreService: AccountKeystoreService,
  private val pref: SharedPreferences,
  private val analyticsSetUp: AnalyticsSetup,
  private val passwordStore: PasswordStore,
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val eipTransactionParser: _EIPTransactionParser,
  private val oneStepTransactionParser: _OneStepTransactionParser,
  @BrokerDefaultRetrofit private val retrofit: Retrofit,
  private val walletInfoRepository: WalletInfoRepository,
  private val brokerVerificationApi: BrokerVerificationRepository.BrokerVerificationApi,
  private val preferences: CarrierBillingPreferencesRepository,
  private val billingAnalytics: BillingAnalytics,
  private val formatter: CurrencyFormatUtils,
  private val logger: Logger,
) {

  companion object {
    private const val METHOD = "onebip"
    private const val NOT_ALLOWED_CODE = "NotAllowed"
    private const val FORBIDDEN_CODE = "Authorization.Forbidden"
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
  }

  private val RETURN_URL = "https://${BuildConfig.APPLICATION_ID}/return/carrier_billing"

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: android.util.Pair<String, ECKey>? = null

  fun present() {
    initializeView()
    onAvailableCountryList()
  }

  private fun onAvailableCountryList() {
    retrieveAvailableCountries()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        if (it.shouldFilter()) {
          view.setState(_FilterCountriesViewState(it.convertListToString(), it.defaultCountry))
        }
//        val phoneNumber = interactor.retrievePhoneNumber()
//        if (phoneNumber != null) view.showSavedPhoneNumber(phoneNumber)
//        else view.hideSavedPhoneNumber()
        view.setState(_PhoneNumberLayoutViewState)
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun initializeView() {
    view.setState(_CarrierVerifyDataViewState(data))
    getApplicationInfo(data.domain)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { ai -> view.setState(_AppDetailsViewState(ai.appName, ai.icon)) }
      .subscribe({}, { e -> e.printStackTrace() })
      .isDisposed
  }

  fun onChangeButtonClick() {
    forgetPhoneNumber()
//    view.hideSavedPhoneNumber(true)
//    view.focusOnPhoneNumber()
  }

  fun onPhoneNumberChange(number: String, isValid: Boolean) {
//    view.setNextButtonEnabled(event.second)
//    view.removePhoneNumberFieldError()
  }

  fun onNextButton(phoneNumberText: String) {
    Observable.fromCallable {
//        view.lockRotation()
//        view.setLoading()
      billingAnalytics.sendActionPaymentMethodDetailsActionEvent(
        data.domain,
        data.skuId,
        data.appcAmount.toString(),
        BillingAnalytics.PAYMENT_METHOD_CARRIER,
        data.transactionType,
        "next"
      )
      phoneNumberText
    }
      .flatMap { phoneNumber ->
        createPayment(
          phoneNumber = phoneNumber,
          packageName = data.domain,
          origin = data.origin,
          transactionData = data.transactionData,
          transactionType = data.transactionType,
          currency = data.currency,
          value = data.fiatAmount.toString()
        )
          .observeOn(AndroidSchedulers.mainThread())
          .flatMapObservable { paymentModel ->
//            view.unlockRotation()
            var completable = Completable.complete()
            if (paymentModel.error !is NoError) {
              completable = handleError(paymentModel)
            } else if (paymentModel.status == TransactionStatus.PENDING_USER_PAYMENT) {
              completable = handleUnknownFeeOrCarrier()
              safeLet(paymentModel.carrier, paymentModel.fee) { carrier, fee ->
                fee.cost?.let { cost ->
                  completable = Completable.fromAction {
                    navigator.navigate(
                      _GoToCarrierFee(
                        uid = paymentModel.uid,
                        domain = data.domain,
                        transactionData = data.transactionData,
                        transactionType = data.transactionType,
                        paymentUrl = paymentModel.paymentUrl!!,
                        currency = data.currency,
                        amount = data.fiatAmount,
                        appcAmount = data.appcAmount,
                        bonus = data.bonusAmount,
                        skuDescription = data.skuDescription,
                        skuId = data.skuId,
                        feeFiatAmount = cost.value,
                        carrierName = carrier.name,
                        carrierImage = carrier.icon,
                        phoneNumber = phoneNumber
                      )
                    )
                  }
                }
              }
            }
            return@flatMapObservable completable.andThen(Observable.just(paymentModel))
          }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnError { handleException(it) }
      .retry()
      .subscribe({}, { handleException(it) })
      .isDisposed
  }

  private fun handleUnknownFeeOrCarrier(): Completable {
    return Completable.fromAction {
      logger.log(CarrierVerifyFragment.TAG, "Unknown fee or carrier")
      navigator.navigate(_GoToError(R.string.activity_iab_error_message))
    }
  }

  private fun handleException(throwable: Throwable) {
    logger.log(CarrierVerifyFragment.TAG, throwable)
    navigator.navigate(_GoToError(R.string.activity_iab_error_message))
  }

  private fun handleError(paymentModel: CarrierPaymentModel): Completable {
    logger.log(CarrierVerifyFragment.TAG, paymentModel.error.errorMessage)
    when (paymentModel.error) {
      is InvalidPhoneNumber -> {
        return Completable.fromAction { view.setState(_InvalidPhoneNumberViewState) }
      }
      is InvalidPriceError -> {
        val error = paymentModel.error as InvalidPriceError
        when (error.type) {
          InvalidPriceError.BoundType.LOWER -> {
            return Completable.fromAction {
              navigator.navigate(
                _GoToError(
                  R.string.purchase_carrier_error_minimum,
                  formatFiatValue(error.value, data.currency)
                )
              )
            }
          }
          InvalidPriceError.BoundType.UPPER -> {
            return Completable.fromAction {
              navigator.navigate(
                _GoToError(
                  R.string.purchase_carrier_error_maximum,
                  formatFiatValue(error.value, data.currency)
                )
              )
            }
          }
        }
      }
      is ForbiddenError -> {
        val error = paymentModel.error as ForbiddenError
        return if (error.type == ForbiddenType.BLOCKED) handleFraudFlow()
        else Completable.fromAction {
          navigator.navigate(
            _GoToError(R.string.subscriptions_error_already_subscribed)
          )
        }
      }
      is GenericError -> {
        return Completable.fromAction {
          navigator.navigate(_GoToError(R.string.activity_iab_error_message))
        }
      }
      else -> return Completable.complete()
    }
  }

  private fun handleFraudFlow(): Completable {
    return getWalletStatus()
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { walletStatus ->
        if (walletStatus.blocked) {
          if (walletStatus.verified) {
            navigator.navigate(
              _GoToError(R.string.purchase_error_wallet_block_code_403)
            )
          } else {
            view.setState(_VerificationViewState())
          }
        } else {
          navigator.navigate(
            _GoToError(R.string.purchase_error_wallet_block_code_403)
          )
        }
      }
      .ignoreElement()
  }

  private fun formatFiatValue(value: BigDecimal, currencyCode: String): String {
    val currencySymbol = Currency.getInstance(currencyCode).symbol
    var scaledBonus = value.stripTrailingZeros()
      .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    var newCurrencyString = currencySymbol
    if (scaledBonus < BigDecimal("0.01")) {
      newCurrencyString = "~$currencySymbol"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    return newCurrencyString + formattedBonus
  }


  fun onBackButton() {
    if (data.preselected) {
      billingAnalytics.sendActionPaymentMethodDetailsActionEvent(
        data.domain, data.skuId,
        data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
        data.transactionType, "cancel"
      )
      navigator.navigate(_FinishWithError)
    } else {
      billingAnalytics.sendActionPaymentMethodDetailsActionEvent(
        data.domain, data.skuId,
        data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
        data.transactionType,
        "back"
      )
      navigator.navigate(_Back)
    }
  }

  fun onOtherPaymentsButton() {
    billingAnalytics.sendActionPaymentMethodDetailsActionEvent(
      data.domain,
      data.skuId, data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
      data.transactionType,
      "other_payments"
    )
    removePreSelectedPaymentMethod()
    navigator.navigate(_Back)
  }

  /**
   * Flatten logic
   */

  private fun retrieveAvailableCountries(): Single<AvailableCountryListModel> {
    return retrieveAvailableCountryList()
  }

  private fun retrieveAvailableCountryList(): Single<AvailableCountryListModel> {
    return api.getAvailableCountryList()
      .map { mapList(it) }
      .onErrorReturn { AvailableCountryListModel() }
  }

  private fun mapList(countryList: CountryListResponse): AvailableCountryListModel {
    return AvailableCountryListModel(countryList.items, countryList.default)
  }

  private fun getApplicationInfo(packageName: String): Single<ApplicationInfoModel> {
    return Single.zip(
      getApplicationName(packageName), getApplicationIcon(packageName)
    ) { appName, appIcon ->
      ApplicationInfoModel(packageName, appName, appIcon)
    }
  }

  private fun getApplicationIcon(packageName: String): Single<Drawable> {
    return Single.just(packageName)
      .map { pkgName -> context.packageManager.getApplicationIcon(pkgName) }
      .subscribeOn(Schedulers.io())
  }

  private fun getApplicationName(packageName: String): Single<String> {
    return Single.just(packageName)
      .map { pkgName ->
        val packageInfo = context.packageManager.getApplicationInfo(pkgName, 0)
        return@map context.packageManager.getApplicationLabel(packageInfo)
          .toString()
      }
      .subscribeOn(Schedulers.io())
  }

  private fun createPayment(
    phoneNumber: String, packageName: String,
    origin: String?, transactionData: String, transactionType: String,
    currency: String,
    value: String
  ): Single<CarrierPaymentModel> {
    return Single.zip(
      getAddresses(packageName),
      getTransactionBuilder(transactionData)
    ) { addrs: WalletAddresses, builder: TransactionBuilder ->
      TransactionDataDetails(addrs, builder)
    }
      .flatMap { details ->
        getCurrentPromoCode().flatMap { promoCode ->
          makePayment(
            walletAddress = details.addrs.userAddress,
            walletSignature = details.addrs.signedAddress,
            phoneNumber = phoneNumber,
            packageName = packageName,
            origin = origin,
            sku = details.builder.skuId,
            reference = details.builder.orderReference,
            transactionType = transactionType,
            currency = currency,
            value = value,
            developerWallet = details.builder.toAddress(),
            entityOemId = details.addrs.entityOemId,
            entityDomain = details.addrs.entityDomain,
            entityPromoCode = promoCode.code,
            userWallet = details.addrs.userAddress,
            referrerUrl = details.builder.referrerUrl,
            developerPayload = details.builder.payload,
            callbackUrl = details.builder.callbackUrl
          )
        }
      }
      .doOnError { logger.log("CarrierInteractor", it) }
  }

  private fun getAddresses(packageName: String): Single<WalletAddresses> {
    return Single.zip(
      getAndSignCurrentWalletAddress()
        .subscribeOn(Schedulers.io()),
      getAttributionEntity(packageName)
        .subscribeOn(Schedulers.io())
    ) { addressModel, attributionEntity ->
      WalletAddresses(
        addressModel.address, addressModel.signedAddress, attributionEntity.oemId,
        attributionEntity.domain
      )
    }
  }

  private fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKey(wallet)
        .map { sign(normalizer.normalize(Keys.toChecksumAddress(wallet.address)), it) }
        .map { WalletAddressModel(wallet.address, it) }
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

  private fun getCurrentWalletAddress() =
    pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)

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

  private fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
  }

  private fun getTransactionBuilder(transactionData: String): Single<TransactionBuilder> {
    return parseTransaction(transactionData, true)
      .subscribeOn(Schedulers.io())
  }

  private fun parseTransaction(uri: String, isBds: Boolean): Single<TransactionBuilder> {
    return if (isBds) {
      bdsParseTransaction(uri)
    } else {
      asfParseTransaction(uri)
    }
  }

  private fun bdsParseTransaction(uri: String): Single<TransactionBuilder> {
    return asfParseTransaction(uri)
  }

  private fun asfParseTransaction(uri: String): Single<TransactionBuilder> {
    return parseTransaction(uri)
  }

  private fun parseTransaction(data: String) = if (data.isEthereumURLString()) {
    Single.just(parseERC681(data))
      .map { erc681 -> eipTransactionParser.buildTransaction(erc681) }
  } else {
    if (Uri.parse(data).isOneStepURLString()) {
      Single.just(parseOneStep(Uri.parse(data)))
        .map { oneStepUri -> oneStepTransactionParser.buildTransaction(oneStepUri, data) }
    } else {
      Single.error(RuntimeException("is not an supported URI"))
    }
  }

  private fun makePayment(
    walletAddress: String,
    walletSignature: String,
    phoneNumber: String,
    packageName: String,
    origin: String?,
    sku: String?,
    reference: String?,
    transactionType: String,
    currency: String,
    value: String,
    developerWallet: String?,
    entityOemId: String?,
    entityDomain: String?,
    entityPromoCode: String?,
    userWallet: String?,
    referrerUrl: String?,
    developerPayload: String?,
    callbackUrl: String?
  ): Single<CarrierPaymentModel> {
    return api.makePayment(
      walletAddress, walletSignature,
      CarrierTransactionBody(
        phoneNumber,
        RETURN_URL,
        METHOD,
        packageName,
        origin,
        sku,
        reference,
        transactionType,
        currency,
        value,
        developerWallet,
        entityOemId,
        entityDomain,
        entityPromoCode,
        userWallet,
        referrerUrl,
        developerPayload,
        callbackUrl
      )
    )
      .map { response -> mapPayment(response) }
      .onErrorReturn { e ->
        logger.log("CarrierBillingRepository", e)
        mapPaymentError(e)
      }
  }

  private fun getCurrentPromoCode(): Single<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
      .firstOrError()
  }

  private fun mapPayment(response: CarrierCreateTransactionResponse): CarrierPaymentModel {
    return CarrierPaymentModel(
      response.uid, null, null, response.url, response.fee,
      response.carrier, null, response.status, NoError
    )
  }

  private fun mapPaymentError(throwable: Throwable): CarrierPaymentModel {
    throwable.printStackTrace()

    val code = if (throwable is HttpException) throwable.code() else null
    val isNoNetworkException = throwable.isNoNetworkException()
    var carrierError: CarrierError = GenericError(isNoNetworkException, code, throwable.message)

    // If we retrieve a specific error from response body, specify the error
    if (throwable is HttpException) {
      throwable.response()
        ?.errorBody()
        ?.let { body ->
          val errorConverter: Converter<ResponseBody, CarrierErrorResponse> = retrofit
            .responseBodyConverter(
              CarrierErrorResponse::class.java,
              arrayOfNulls<Annotation>(0)
            )
          val bodyErrorResponse = try {
            errorConverter.convert(body)
          } catch (e: Exception) {
            e.printStackTrace()
            null
          } finally {
            body.close()
          }
          carrierError = mapErrorResponseToCarrierError(code, bodyErrorResponse)
            ?: carrierError
        }
    }

    return CarrierPaymentModel(carrierError)
  }

  private fun mapErrorResponseToCarrierError(
    httpCode: Int?,
    response: CarrierErrorResponse?
  ): CarrierError? {
    val errorType = mapForbiddenCode(response?.code)
    if (errorType != null) {
      return ForbiddenError(httpCode, response?.text, errorType)
    }

    if (response?.data == null || response.data.isEmpty()) {
      return null
    }

    val error = response.data[0]
    when (response.code) {
      "Body.Fields.Invalid" -> {
        if (error.name == "phone_number") {
          return InvalidPhoneNumber(
            httpCode,
            error.messages?.technical
          )
        }
      }
      "Resource.Gateways.Dimoco.Transactions.InvalidPrice" -> {
        val type = when (error.type) {
          "UPPER_BOUND" -> InvalidPriceError.BoundType.UPPER
          "LOWER_BOUND" -> InvalidPriceError.BoundType.LOWER
          else -> null
        }
        if (type != null && error.value != null) {
          return InvalidPriceError(httpCode, response.text, type, error.value)
        }
      }
    }
    return null
  }

  private fun mapForbiddenCode(responseCode: String?): ForbiddenType? {
    return when (responseCode) {
      NOT_ALLOWED_CODE -> ForbiddenType.SUB_ALREADY_OWNED
      FORBIDDEN_CODE -> ForbiddenType.BLOCKED
      else -> null
    }
  }

  private fun getWalletStatus(): Single<WalletStatus> {
    return Single.zip(
      isWalletBlocked().subscribeOn(Schedulers.io()),
      isWalletVerified().subscribeOn(Schedulers.io())
    ) { blocked, verified -> WalletStatus(blocked, verified) }
  }

  fun isWalletBlocked(): Single<Boolean> {
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

  private fun isWalletVerified(): Single<Boolean> =
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

  fun forgetPhoneNumber() = preferences.forgetPhoneNumber()

  fun removePreSelectedPaymentMethod() {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.remove(InAppPurchaseInteractor.PRE_SELECTED_PAYMENT_METHOD_KEY)
    editor.apply()
  }

}

data class ApplicationInfoModel(val packageName: String, val appName: String, val icon: Drawable)

data class AvailableCountryListModel(
  val countryList: List<String>,
  val defaultCountry: String?,
  val hasError: Boolean = false
) {
  constructor() : this(emptyList(), null, true)

  fun convertListToString(): String {
    return countryList.joinToString(",") { country -> country.toLowerCase(Locale.ROOT) }
  }

  fun shouldFilter(): Boolean {
    return !hasError && countryList.isNotEmpty()
  }
}
