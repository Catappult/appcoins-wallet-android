package com.asfoundation.wallet.ui.iab

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import com.appcoins.wallet.appcoins.rewards.ResponseErrorBaseBody
import com.appcoins.wallet.appcoins.rewards.getMessage
import com.appcoins.wallet.bdsbilling.PaymentProof
import com.appcoins.wallet.bdsbilling.SubscriptionPurchaseResponse
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType.Companion.valueOfInsensitive
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType.Companion.valueOfManagedType
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Companion.notFound
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.commons.Repository
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import com.asfoundation.wallet.entity.*
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.repository.BdsTransactionService.BdsTransaction
import com.asfoundation.wallet.repository.PaymentTransaction.PaymentState
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.TokenRateService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor.CurrentPaymentStep
import com.asfoundation.wallet.ui.iab._IabLogic.Companion.PRE_SELECTED_PAYMENT_METHOD_KEY
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import com.google.gson.Gson
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
import org.kethereum.erc681.isEthereumURLString
import org.kethereum.erc681.parseERC681
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.math.BigInteger
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("LABEL_NAME_CLASH")
class _OnChainBuyLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val isBds: Boolean,
  private val analytics: BillingAnalytics,
  private val appPackage: String,
  private val uriString: String,
  private val logger: Logger,
  private val eipTransactionParser: _EIPTransactionParser,
  private val oneStepTransactionParser: _OneStepTransactionParser,
  private val asfPaymentTransactionCache: Repository<String, PaymentTransaction>,
  private val bdsPaymentTransactionCache: Repository<String, PaymentTransaction>,
  private val asfWatchedTransactionCache: Repository<String, com.asfoundation.wallet.repository.Transaction>,
  private val bdsWatchedTransactionCache: Repository<String, com.asfoundation.wallet.repository.Transaction>,
  private val asfBuyWatchedTransactionCache: Repository<String, com.asfoundation.wallet.repository.Transaction>,
  private val bdsBuyWatchedTransactionCache: Repository<String, com.asfoundation.wallet.repository.Transaction>,
  private val bdsTrackTransactionCache: Repository<String, BdsTransaction>,
  private val transactionFromApprove: MutableMap<String, com.appcoins.wallet.bdsbilling.repository.entity.Transaction>,
  private val transactionIdsFromBuy: MutableMap<String, String>,
  private var pref: SharedPreferences,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private val transactionBuilder: TransactionBuilder,
  private val gasSettingsRepository: GasSettingsRepository,
  private val defaultTokenRepository: _DefaultTokenRepository,
  private val web3jProvider: Web3jProvider,
  private val walletInfoRepository: WalletInfoRepository,
  private val passwordStore: PasswordStore,
  private val brokerBdsApi: RemoteRepository.BrokerBdsApi,
  private val defaultNetwork: NetworkInfo,
  private val nonceObtainer: MultiWalletNonceObtainer,
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val supportRepository: SupportRepository,
  private val brokerVerificationApi: BrokerVerificationRepository.BrokerVerificationApi,
  private val tokenRateService: TokenRateService,
  private val countryCodeProvider: CountryCodeProvider,
  private val inappBdsApi: RemoteRepository.InappBdsApi,
  private val subsApi: SubscriptionBillingApi,
  private val gson: Gson,
) {

  private val defaultStoreAddress: String = BuildConfig.DEFAULT_STORE_ADDRESS
  private val defaultOemAddress: String = BuildConfig.DEFAULT_OEM_ADDRESS

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: Pair<String, ECKey>? = null

  fun invoke(productName: String, amount: BigDecimal, developerPayload: String) {
    setupUi(amount, developerPayload)
    onBuyEvent(productName, developerPayload, isBds)
  }

  private fun showTransactionState() {
    getTransactionState(uriString)
      .observeOn(AndroidSchedulers.mainThread())
      .flatMapCompletable { showPendingTransaction(it) }
      .subscribe({}) { showError(it) }
      .isDisposed
  }

  private fun onBuyEvent(productName: String, developerPayload: String, isBds: Boolean) {
    showTransactionState()
    send(
      uri = uriString,
      transactionType = AsfInAppPurchaseInteractor.TransactionType.NORMAL,
      packageName = appPackage,
      productName = productName,
      developerPayload = developerPayload,
      isBds = isBds,
      transactionBuilder = transactionBuilder
    )
      .observeOn(AndroidSchedulers.mainThread())
      .doOnError { showError(it) }
      .subscribe({}, { showError(it) })
      .isDisposed
  }

  fun onOkErrorClick() {
    parseTransaction(uriString, isBds)
      .subscribe({ close() }, { close() })
      .isDisposed
  }

  fun onSupportClick(gamificationLevel: Int) {
    showSupport(gamificationLevel)
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun setupUi(appcAmount: BigDecimal, developerPayload: String?) {
    parseTransaction(uriString, isBds)
      .flatMapCompletable { transaction: TransactionBuilder ->
        getCurrentPaymentStep(appPackage, transaction)
          .flatMapCompletable { currentPaymentStep: CurrentPaymentStep ->
            when (currentPaymentStep) {
              CurrentPaymentStep.PAUSED_ON_CHAIN -> resume(
                uri = uriString,
                transactionType = AsfInAppPurchaseInteractor.TransactionType.NORMAL,
                packageName = appPackage,
                productName = transaction.skuId,
                developerPayload = developerPayload,
                isBds = isBds,
                type = transaction.type,
                transactionBuilder = transactionBuilder
              )

              CurrentPaymentStep.READY -> Completable.fromAction { setup(appcAmount) }
                .subscribeOn(AndroidSchedulers.mainThread())

              CurrentPaymentStep.NO_FUNDS -> Completable.fromAction {
                view.setState(_NoFundsErrorViewState)
              }
                .subscribeOn(AndroidSchedulers.mainThread())

              CurrentPaymentStep.PAUSED_CC_PAYMENT,
              CurrentPaymentStep.PAUSED_LOCAL_PAYMENT,
              CurrentPaymentStep.PAUSED_CREDITS ->
                Completable.error(
                  UnsupportedOperationException(
                    "Cannot resume from " + currentPaymentStep.name + " status"
                  )
                )
            }
          }
      }
      .subscribe({}) { showError(it) }
      .isDisposed
  }

  private fun close() = navigator.navigate(_Close(mapCancellation()))

  private fun showError(throwable: Throwable?, message: String? = null, userMessage: Int? = null) {
    logger.log(TAG, message, throwable)
    if (throwable is UnknownTokenException) view.setState(_WrongNetworkErrorViewState)
    else view.setState(_ErrorViewState())
  }

  private fun showPendingTransaction(transaction: Payment): Completable {
    sendPaymentErrorEvent(transaction)
    return when (transaction.status) {
      Payment.Status.COMPLETED -> {
        getCompletedPurchase(transaction, isBds)
          .observeOn(AndroidSchedulers.mainThread())
          .map { buildBundle(it, transaction.orderReference) }
          .flatMapCompletable { bundle -> handleSuccessTransaction(bundle) }
          .onErrorResumeNext { Completable.fromAction { showError(it) } }
      }
      Payment.Status.NO_FUNDS -> Completable.fromAction { view.setState(_NoFundsErrorViewState) }
        .andThen(remove(transaction.uri))

      Payment.Status.NETWORK_ERROR -> Completable.fromAction {
        view.setState(
          _WrongNetworkErrorViewState
        )
      }
        .andThen(remove(transaction.uri))

      Payment.Status.NO_TOKENS -> Completable.fromAction { view.setState(_NoTokenFundsErrorViewState) }
        .andThen(remove(transaction.uri))

      Payment.Status.NO_ETHER -> Completable.fromAction { view.setState(_NoEtherFundsErrorViewState) }
        .andThen(remove(transaction.uri))

      Payment.Status.NO_INTERNET -> Completable.fromAction { view.setState(_NoNetworkErrorViewState) }
        .andThen(remove(transaction.uri))

      Payment.Status.NONCE_ERROR -> Completable.fromAction { view.setState(_NonceErrorViewState) }
        .andThen(remove(transaction.uri))

      Payment.Status.APPROVING -> Completable.fromAction { view.setState(_ApprovingViewState) }

      Payment.Status.BUYING -> Completable.fromAction { view.setState(_BuyingViewState) }
      Payment.Status.FORBIDDEN -> Completable.fromAction { onFraudFlow() }
        .andThen(remove(transaction.uri))
      Payment.Status.SUB_ALREADY_OWNED -> Completable.fromAction {
        showError(null, "Sub Already Owned", R.string.subscriptions_error_already_subscribed)
      }
      Payment.Status.ERROR -> Completable.fromAction {
        showError(null, "Payment status: ${transaction.status.name}")
      }
        .andThen(remove(transaction.uri))

      else -> Completable.fromAction {
        showError(null, "Payment status: UNKNOWN")
      }
        .andThen(remove(transaction.uri))
    }
  }

  private fun handleSuccessTransaction(bundle: Bundle): Completable =
    Completable.fromAction { view.setState(_TransactionCompletedViewState) }
      .subscribeOn(AndroidSchedulers.mainThread())
      .andThen(
        Completable.timer(
          -1 /* lottieTransactionComplete.getDuration() */,
          TimeUnit.MILLISECONDS
        )
      )
      .andThen(Completable.fromRunnable {
        sendPaymentEvent()
        sendRevenueEvent()
        sendPaymentSuccessEvent()
        bundle.putString(
          PRE_SELECTED_PAYMENT_METHOD_KEY,
          PaymentMethodsView.PaymentMethodId.APPC.id
        )
        navigator.navigate(_Finish(bundle))
      })

  private fun buildBundle(payment: Payment, orderReference: String?): Bundle {
    return if (payment.uid != null && payment.signature != null && payment.signatureData != null) {
      mapPurchase(
        payment.uid!!,
        payment.signature!!,
        payment.signatureData!!,
        orderReference
      )
    } else {
      Bundle().also {
        it.putInt(IabActivity.RESPONSE_CODE, 0)
        it.putString(IabActivity.TRANSACTION_HASH, payment.buyHash)
      }
    }
  }

  private fun setup(amount: BigDecimal) =
    view.setState(
      _RaidenChannelValuesViewState(
        amount.add(BigDecimal(5).subtract(amount.remainder(BigDecimal(5))))
          .let {
            listOf(
              amount,
              it,
              it.add(BigDecimal(5)),
              it.add(BigDecimal(15)),
              it.add(BigDecimal(25))
            )
          }
      )
    )

  fun sendPaymentEvent() {
    analytics.sendPaymentEvent(
      appPackage,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      BillingAnalytics.PAYMENT_METHOD_APPC,
      transactionBuilder.type
    )
  }

  fun sendRevenueEvent() {
    tokenRateService.getAppcRate(BillingAnalytics.EVENT_REVENUE_CURRENCY)
      .map { fiatValueConversion: FiatValue ->
        FiatValue(
          fiatValueConversion.amount.multiply(
            BigDecimal.valueOf(transactionBuilder.amount().toDouble())
          ),
          fiatValueConversion.currency,
          fiatValueConversion.symbol
        )
      }
      .doOnSuccess { (amount) -> analytics.sendRevenueEvent(amount.toString()) }
      .subscribe({ }, { it.printStackTrace() })
      .isDisposed
  }

  fun sendPaymentSuccessEvent() {
    analytics.sendPaymentSuccessEvent(
      appPackage, transactionBuilder.skuId,
      transactionBuilder.amount()
        .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transactionBuilder.type
    )
  }

  private fun sendPaymentErrorEvent(payment: Payment) {
    val status = payment.status
    if (isError(status)) {
      if (payment.errorCode == null && payment.errorMessage == null) {
        analytics.sendPaymentErrorEvent(
          appPackage, transactionBuilder.skuId,
          transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transactionBuilder.type,
          status.name
        )

      } else {
        analytics.sendPaymentErrorWithDetailsEvent(
          appPackage, transactionBuilder.skuId,
          transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transactionBuilder.type,
          payment.errorCode.toString(), payment.errorMessage.toString()
        )

      }
    }
  }

  private fun onFraudFlow() {
    isWalletBlocked()
      .subscribeOn(Schedulers.io())
      .observeOn(Schedulers.io())
      .flatMap { blocked ->
        if (blocked) {
          isWalletVerified()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { verified ->
              if (verified) {
                view.setState(_ForbiddenErrorViewState)
              } else {
                view.setState(_VerificationViewState())
              }
            }
        } else {
          Single.just(true)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { view.setState(_ForbiddenErrorViewState) }
        }
      }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({}, {
        logger.log(TAG, it)
        view.setState(_ForbiddenErrorViewState)
      })
      .isDisposed
  }

  private fun isError(status: Payment.Status): Boolean =
    status == Payment.Status.ERROR || status == Payment.Status.NO_FUNDS ||
        status == Payment.Status.NONCE_ERROR || status == Payment.Status.NO_ETHER ||
        status == Payment.Status.NO_INTERNET || status == Payment.Status.NO_TOKENS ||
        status == Payment.Status.NETWORK_ERROR || status == Payment.Status.FORBIDDEN ||
        status == Payment.Status.SUB_ALREADY_OWNED

  /**
   * Flatten logic
   */

  private fun send(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String,
    isBds: Boolean,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return if (isBds) {
      bdsSend(
        uri = uri,
        transactionType = transactionType,
        packageName = packageName,
        productName = productName,
        developerPayload = developerPayload,
        transactionBuilder = transactionBuilder,
      )
    } else {
      asfSend(
        uri = uri,
        transactionType = transactionType,
        packageName = packageName,
        productName = productName,
        developerPayload = developerPayload,
        transactionBuilder = transactionBuilder,
        isBds = false,
      )
    }
  }

  private fun bdsSend(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String,
    transactionBuilder: TransactionBuilder,
  ): Completable {
    return asfSend(
      uri = uri,
      transactionType = transactionType,
      packageName = packageName,
      productName = productName,
      developerPayload = developerPayload,
      transactionBuilder = transactionBuilder,
      isBds = true
    )
  }

  private fun asfSend(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String,
    transactionBuilder: TransactionBuilder,
    isBds: Boolean
  ): Completable {
    return if (transactionType == AsfInAppPurchaseInteractor.TransactionType.NORMAL) {
      buildPaymentTransaction(
        uri = uri,
        packageName = packageName,
        productName = productName,
        developerPayload = developerPayload,
        amount = transactionBuilder.amount()
      ).flatMapCompletable { paymentTransaction: PaymentTransaction ->
        send(
          paymentTransaction.uri,
          paymentTransaction,
          isBds
        )
      }
    } else Completable.error(
      java.lang.UnsupportedOperationException(
        "Transaction type $transactionType not supported"
      )
    )
  }

  private fun buildPaymentTransaction(
    uri: String,
    packageName: String,
    productName: String,
    developerPayload: String,
    amount: BigDecimal
  ): Single<PaymentTransaction> {
    return Single.zip(
      parseTransaction(uri).observeOn(Schedulers.io()),
      find().observeOn(Schedulers.io())
    ) { transaction: TransactionBuilder, wallet: Wallet ->
      transaction.fromAddress(
        wallet.address
      )
    }
      .flatMap { transactionBuilder: TransactionBuilder ->
        fetch(true)
          .map { gasSettings: GasSettings ->
            transactionBuilder.gasSettings(
              GasSettings(
                gasSettings.gasPrice,
                paymentGasLimit
              )
            )
            transactionBuilder.amount(amount)
          }
      }
      .map { transactionBuilder: TransactionBuilder ->
        PaymentTransaction(
          uri, transactionBuilder, packageName,
          productName, transactionBuilder.skuId, developerPayload,
          transactionBuilder.callbackUrl, transactionBuilder.orderReference
        )
      }
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

  private fun fetch(forTokenTransfer: Boolean): Single<GasSettings> {
    return gasSettingsRepository.getGasSettings(forTokenTransfer)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

  private fun send(
    key: String,
    paymentTransaction: PaymentTransaction,
    isBds: Boolean,
  ): Completable {
    return checkFunds(
      key,
      paymentTransaction,
      checkAllowance(key, paymentTransaction, isBds),
      isBds
    )
  }

  private fun checkFunds(
    key: String,
    paymentTransaction: PaymentTransaction,
    action: Completable,
    isBds: Boolean,
  ): Completable {
    return Completable.fromAction {
      if (isBds) {
        bdsPaymentTransactionCache.saveSync(key, paymentTransaction)
      } else {
        asfPaymentTransactionCache.saveSync(key, paymentTransaction)
      }
    }
      .andThen(getBalanceState(paymentTransaction.transactionBuilder)
        .observeOn(Schedulers.io())
        .flatMapCompletable { balance ->
          when (balance) {
            BalanceState.NO_TOKEN ->
              return@flatMapCompletable asfPaymentTransactionCache.save(
                key, PaymentTransaction(
                  paymentTransaction,
                  PaymentState.NO_TOKENS
                )
              )
            BalanceState.NO_ETHER ->
              return@flatMapCompletable asfPaymentTransactionCache.save(
                key, PaymentTransaction(
                  paymentTransaction,
                  PaymentState.NO_ETHER
                )
              )
            BalanceState.NO_ETHER_NO_TOKEN ->
              return@flatMapCompletable asfPaymentTransactionCache.save(
                key, PaymentTransaction(
                  paymentTransaction,
                  PaymentState.NO_FUNDS
                )
              )
            BalanceState.OK -> return@flatMapCompletable action
            else -> return@flatMapCompletable action
          }
        }
      )
      .onErrorResumeNext { throwable ->
        val (paymentState, errorCode, errorMessage) = map(throwable)
        asfPaymentTransactionCache.save(
          paymentTransaction.uri,
          PaymentTransaction(paymentTransaction, paymentState, errorCode, errorMessage)
        )
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

  private fun map(throwable: Throwable): PaymentError {
    throwable.printStackTrace()
    return when (throwable) {
      is HttpException -> mapHttpException(throwable)
      is UnknownHostException -> PaymentError(PaymentState.NO_INTERNET)
      is WrongNetworkException -> PaymentError(PaymentState.WRONG_NETWORK)
      is TransactionNotFoundException -> PaymentError(PaymentState.ERROR)
      is UnknownTokenException -> PaymentError(PaymentState.UNKNOWN_TOKEN)
      is TransactionException -> mapTransactionException(throwable)
      else -> PaymentError(PaymentState.ERROR, null, throwable.message)
    }
  }

  private fun mapHttpException(exception: HttpException): PaymentError {
    return if (exception.code() == FORBIDDEN_CODE) {
      val messageInfo = gson.fromJson(exception.getMessage(), ResponseErrorBaseBody::class.java)
      when (messageInfo.code) {
        "NotAllowed" -> PaymentError(PaymentState.SUB_ALREADY_OWNED)
        "Authorization.Forbidden" -> PaymentError(PaymentState.FORBIDDEN)
        else -> PaymentError(PaymentState.ERROR)
      }
    } else {
      val message = exception.getMessage()
      PaymentError(PaymentState.ERROR, exception.code(), message)
    }
  }

  private fun mapTransactionException(throwable: Throwable): PaymentError {
    return when (throwable.message) {
      INSUFFICIENT_ERROR_MESSAGE -> PaymentError(PaymentState.NO_FUNDS)
      NONCE_TOO_LOW_ERROR_MESSAGE -> PaymentError(PaymentState.NONCE_ERROR)
      else -> PaymentError(PaymentState.ERROR, null, throwable.message)
    }
  }

  private fun checkAllowance(
    key: String,
    paymentTransaction: PaymentTransaction,
    isBds: Boolean
  ): Completable {
    val transactionBuilder = paymentTransaction.transactionBuilder
    val fromAddress = transactionBuilder.fromAddress()
    val contractAddress = transactionBuilder.iabContract
    val tokenAddress = transactionBuilder.contractAddress()
    return checkAllowance(fromAddress, contractAddress, tokenAddress)
      .flatMapCompletable { allowance: BigDecimal ->
        if (allowance.compareTo(BigDecimal.ZERO) == 0) {
          return@flatMapCompletable approve(key, paymentTransaction, isBds)
        } else {
          val approveWithZeroPaymentTransaction: PaymentTransaction =
            createApproveZeroTransaction(paymentTransaction)
          return@flatMapCompletable approveWithoutValidation(
            key + "zero",
            approveWithZeroPaymentTransaction.transactionBuilder,
            isBds
          )
            .andThen(
              getApprove(key + "zero", isBds)
                .filter { approveTransaction -> approveTransaction.status == ApproveService.Status.APPROVED }
                .take(1)
                .ignoreElements()
            )
            .andThen(approve(key, paymentTransaction, isBds))
        }
      }
  }

  private fun checkAllowance(
    owner: String,
    spender: String,
    tokenAddress: String
  ): Single<BigDecimal> {
    return getDefaultToken()
      .map { tokenInfo: TokenInfo ->
        val function = allowance(owner, spender)
        val responseValue = callSmartContractFunction(function, tokenAddress, owner)
        val response = FunctionReturnDecoder.decode(responseValue, function.outputParameters)

        if (response.size == 1) {
          BigDecimal((response[0] as Uint256).value)
            .multiply(BigDecimal(BigInteger.ONE, tokenInfo.decimals))
        } else {
          throw IllegalStateException("Failed to execute contract call!")
        }
      }
  }

  private fun allowance(owner: String, spender: String): org.web3j.abi.datatypes.Function {
    return org.web3j.abi.datatypes.Function(
      "allowance",
      listOf(Address(owner), Address(spender)),
      listOf(object : TypeReference<Uint256?>() {})
    )
  }

  @Throws(Exception::class)
  private fun callSmartContractFunction(
    function: org.web3j.abi.datatypes.Function,
    contractAddress: String,
    walletAddress: String
  ): String {
    val encodedFunction = FunctionEncoder.encode(function)
    val transaction =
      org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
        walletAddress,
        contractAddress,
        encodedFunction
      )
    return web3jProvider.default.ethCall(transaction, DefaultBlockParameterName.LATEST)
      .send()
      .value
  }

  private fun createApproveZeroTransaction(paymentTransaction: PaymentTransaction): PaymentTransaction {
    val transactionBuilder = paymentTransaction.transactionBuilder
    val approveWithZeroTransactionBuilder: TransactionBuilder =
      copyTransactionBuilder(transactionBuilder)
    approveWithZeroTransactionBuilder.amount(BigDecimal.ZERO)
    return PaymentTransaction(paymentTransaction, approveWithZeroTransactionBuilder)
  }

  private fun copyTransactionBuilder(transactionBuilder: TransactionBuilder): TransactionBuilder {
    return TransactionBuilder(transactionBuilder)
  }

  private fun approve(
    key: String,
    paymentTransaction: PaymentTransaction,
    isBds: Boolean
  ): Completable {
    return if (isBds) {
      bdsValidate(paymentTransaction)
    } else {
      asfValidate(paymentTransaction)
    }
      .flatMapCompletable {
        sendTransaction(
          key,
          paymentTransaction.transactionBuilder,
          isBds
        )
      }
  }

  private fun sendTransaction(
    key: String,
    transactionBuilder: TransactionBuilder,
    isBds: Boolean
  ): Completable {
    return if (isBds) {
      bdsWatchedTransactionCache
    } else {
      asfWatchedTransactionCache
    }.save(
      key,
      com.asfoundation.wallet.repository.Transaction(
        key,
        com.asfoundation.wallet.repository.Transaction.Status.PENDING,
        transactionBuilder
      )
    )
  }

  private fun approveWithoutValidation(
    key: String,
    transactionBuilder: TransactionBuilder,
    isBds: Boolean
  ): Completable {
    return sendTransaction(key, transactionBuilder, isBds)
  }

  private fun getApprove(uri: String, isBds: Boolean): Observable<ApproveTransaction> {
    return getTransaction(uri, isBds)
      .map { map(it) }
  }

  private fun map(transaction: com.asfoundation.wallet.repository.Transaction): ApproveTransaction {
    return ApproveTransaction(
      transaction.key,
      mapTransactionState(transaction.status),
      transaction.transactionHash
    )
  }

  private fun mapTransactionState(status: com.asfoundation.wallet.repository.Transaction.Status): ApproveService.Status {
    val toReturn: ApproveService.Status = when (status) {
      com.asfoundation.wallet.repository.Transaction.Status.PENDING -> ApproveService.Status.PENDING
      com.asfoundation.wallet.repository.Transaction.Status.PROCESSING -> ApproveService.Status.APPROVING
      com.asfoundation.wallet.repository.Transaction.Status.COMPLETED -> ApproveService.Status.APPROVED
      com.asfoundation.wallet.repository.Transaction.Status.ERROR -> ApproveService.Status.ERROR
      com.asfoundation.wallet.repository.Transaction.Status.WRONG_NETWORK -> ApproveService.Status.WRONG_NETWORK
      com.asfoundation.wallet.repository.Transaction.Status.NONCE_ERROR -> ApproveService.Status.NONCE_ERROR
      com.asfoundation.wallet.repository.Transaction.Status.UNKNOWN_TOKEN -> ApproveService.Status.UNKNOWN_TOKEN
      com.asfoundation.wallet.repository.Transaction.Status.NO_TOKENS -> ApproveService.Status.NO_TOKENS
      com.asfoundation.wallet.repository.Transaction.Status.NO_ETHER -> ApproveService.Status.NO_ETHER
      com.asfoundation.wallet.repository.Transaction.Status.NO_FUNDS -> ApproveService.Status.NO_FUNDS
      com.asfoundation.wallet.repository.Transaction.Status.NO_INTERNET -> ApproveService.Status.NO_INTERNET
      com.asfoundation.wallet.repository.Transaction.Status.FORBIDDEN -> ApproveService.Status.FORBIDDEN
      com.asfoundation.wallet.repository.Transaction.Status.SUB_ALREADY_OWNED -> ApproveService.Status.SUB_ALREADY_OWNED
    }
    return toReturn
  }

  private fun getTransaction(
    key: String,
    isBds: Boolean
  ): Observable<com.asfoundation.wallet.repository.Transaction> =
    if (isBds) {
      bdsWatchedTransactionCache
    } else {
      asfWatchedTransactionCache
    }.get(key)
      .filter { it.status != com.asfoundation.wallet.repository.Transaction.Status.PENDING }

  private fun asfValidate(
    @Suppress("UNUSED_PARAMETER") paymentTransaction: PaymentTransaction
  ): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> {
    return Single.just(notFound())
  }

  private fun bdsValidate(paymentTransaction: PaymentTransaction): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> {
    val packageName = paymentTransaction.packageName
    val productName = paymentTransaction.transactionBuilder
      .skuId
    val getTransactionHash: Single<String> = getDefaultToken()
      .flatMap {
        computeBuyTransactionHash(paymentTransaction.transactionBuilder)
      }
    val attributionEntity = getAttributionEntity(packageName)
    return Single.zip(
      getTransactionHash, attributionEntity
    ) { hash: String?, (oemId, domain): AttributionEntity ->
      PaymentProof(
        "appcoins",
        paymentTransaction.approveHash!!,
        hash!!, productName, packageName, oemId, domain
      )
    }
      .flatMap { paymentProof ->
        processPurchaseProof(paymentProof)
      }
  }

  private fun sendBuyTransaction(
    key: String,
    transactionBuilder: TransactionBuilder,
    isBds: Boolean
  ): Completable {
    return if (isBds) {
      bdsBuyWatchedTransactionCache
    } else {
      asfBuyWatchedTransactionCache
    }.save(
      key,
      com.asfoundation.wallet.repository.Transaction(
        key,
        com.asfoundation.wallet.repository.Transaction.Status.PENDING,
        transactionBuilder
      )
    )
  }

  private fun computeBuyTransactionHash(transactionBuilder: TransactionBuilder): Single<String> {
    return passwordStore.getPassword(transactionBuilder.fromAddress())
      .flatMap { password -> computeBuyTransactionHash(transactionBuilder, password) }
  }

  private fun computeBuyTransactionHash(
    transactionBuilder: TransactionBuilder,
    password: String
  ): Single<String> {
    return getDefaultToken()
      .observeOn(Schedulers.io())
      .flatMap {
        createRawTransaction(
          transactionBuilder,
          password,
          transactionBuilder.appcoinsData(),
          transactionBuilder.iabContract,
          BigDecimal.ZERO,
          nonceObtainer.getNonce(
            Address(transactionBuilder.fromAddress()),
            getChainId(transactionBuilder)
          )
        )
      }
      .map { signedTx -> calculateHashFromSigned(signedTx) }
  }

  private fun createRawTransaction(
    transactionBuilder: TransactionBuilder,
    password: String,
    data: ByteArray,
    toAddress: String,
    amount: BigDecimal,
    nonce: BigInteger
  ): Single<ByteArray> {
    return Single.just(nonce)
      .flatMap {
        if (transactionBuilder.chainId != TransactionBuilder.NO_CHAIN_ID
          && transactionBuilder.chainId != defaultNetwork.chainId.toLong()
        ) {
          var requestedNetwork = "unknown"
          if (transactionBuilder.chainId == 1L) {
            requestedNetwork = C.ETHEREUM_NETWORK_NAME
          } else if (transactionBuilder.chainId == 3L) {
            requestedNetwork = C.ROPSTEN_NETWORK_NAME
          }
          return@flatMap Single.error<ByteArray>(
            WrongNetworkException(
              """
              Default network is different from the intended on transaction
              Current network: ${defaultNetwork.name}
              Requested: $requestedNetwork
              """.trimIndent()
            )
          )
        }
        accountKeystoreService.signTransaction(
          transactionBuilder.fromAddress(), password,
          toAddress, amount, transactionBuilder.gasSettings().gasPrice,
          transactionBuilder.gasSettings().gasLimit, nonce.toLong(), data,
          defaultNetwork.chainId.toLong()
        )
      }
  }

  private fun getChainId(transactionBuilder: TransactionBuilder): Long {
    return if (transactionBuilder.chainId == TransactionBuilder.NO_CHAIN_ID) {
      defaultNetwork.chainId.toLong()
    } else {
      transactionBuilder.chainId
    }
  }

  private fun calculateHashFromSigned(signedTx: ByteArray): String? {
    val hash = Hash.sha3(signedTx)
    return Numeric.toHexString(hash)
  }

  private fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
  }

  private fun processPurchaseProof(paymentProof: PaymentProof): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> =
    transactionFromApprove[paymentProof.approveProof]?.let { transaction ->
      registerPaymentProof(
        transaction.uid,
        paymentProof.paymentProof,
        paymentProof.paymentType
      ).andThen(Single.just(transaction))
    } ?: Single.error(IllegalArgumentException("No payment id for {${paymentProof.approveProof}}"))

  private fun registerPaymentProof(
    paymentId: String,
    paymentProof: String,
    paymentType: String
  ): Completable =
    getWalletAddress().observeOn(Schedulers.io())
      .flatMapCompletable { walletAddress ->
        signContent(walletAddress).observeOn(Schedulers.io())
          .flatMapCompletable { signedData ->
            registerPaymentProof(
              paymentId,
              paymentType,
              walletAddress,
              signedData,
              paymentProof
            )
          }
      }
      .andThen(Completable.fromAction { transactionIdsFromBuy[paymentProof] = paymentId })

  private fun getWalletAddress(): Single<String> = find()
    .map { Keys.toChecksumAddress(it.address) }

  private fun signContent(content: String): Single<String> = find()
    .flatMap { wallet -> getPrivateKey(wallet).map { sign(normalizer.normalize(content), it) } }

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
        .doOnSuccess { ecKey -> stringECKeyPair = Pair(wallet.address, ecKey) }
    }

  @Throws(Exception::class)
  private fun sign(plainText: String, ecKey: ECKey): String =
    ecKey.sign(HashUtil.sha3(plainText.toByteArray())).toHex()

  private fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    walletSignature: String,
    paymentProof: String
  ): Completable =
    brokerBdsApi.patchTransaction(
      paymentType,
      paymentId,
      walletAddress,
      walletSignature,
      paymentProof
    )

  private fun parseTransaction(uri: String, isBds: Boolean): Single<TransactionBuilder> {
    return if (isBds) {
      bdsParseTransaction(uri)
    } else {
      asfParseTransaction(uri)
    }
  }

  private fun mapCancellation(): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_USER_CANCELED)
    return bundle
  }

  private fun bdsParseTransaction(uri: String): Single<TransactionBuilder> {
    return asfParseTransaction(uri)
  }

  private fun asfParseTransaction(uri: String): Single<TransactionBuilder> {
    return parseTransaction(uri)
  }

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
        Intercom.client()
          .logout()
      }
      supportRepository.saveNewUser(address, level)
    }
  }

  private fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
      .displayMessenger()
  }

  private fun getCurrentPaymentStep(
    packageName: String,
    transactionBuilder: TransactionBuilder
  ): Single<CurrentPaymentStep> {
    return Single.zip(
      getTransaction(packageName, transactionBuilder.skuId, transactionBuilder.type),
      isAppcoinsPaymentReady(transactionBuilder)
    ) { transaction, isBuyReady -> this.map(transaction, isBuyReady) }
  }

  private fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKey(wallet)
        .map { sign(normalizer.normalize(Keys.toChecksumAddress(wallet.address)), it) }
        .map { WalletAddressModel(wallet.address, it) }
    }

  private fun getTransaction(
    packageName: String,
    productName: String,
    type: String
  ): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> {
    return Single.defer {
      val billingType = valueOfInsensitive(type)
      getSkuTransaction(
        packageName,
        productName,
        Schedulers.io(),
        billingType
      )
    }
  }

  private fun getSkuTransaction(
    merchantName: String,
    sku: String?,
    scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        getSkuTransaction(merchantName, sku, it.address, it.signedAddress, type)
      }
  }

  private fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<com.appcoins.wallet.bdsbilling.repository.entity.Transaction> =
    brokerBdsApi.getSkuTransaction(
      walletAddress,
      walletSignature,
      0,
      type,
      1,
      "latest",
      false,
      skuId,
      packageName
    )
      .flatMap {
        if (it.items.isNotEmpty()) {
          return@flatMap Single.just(it.items[0])
        }
        return@flatMap Single.just(notFound())
      }

  private fun isAppcoinsPaymentReady(transactionBuilder: TransactionBuilder): Single<Boolean> {
    return fetch(true)
      .doOnSuccess { gasSettings: GasSettings ->
        transactionBuilder.gasSettings(
          GasSettings(
            gasSettings.gasPrice,
            paymentGasLimit
          )
        )
      }
      .flatMap {
        hasBalanceToBuy(
          transactionBuilder
        )
      }
  }

  private fun hasBalanceToBuy(transactionBuilder: TransactionBuilder): Single<Boolean> {
    return getBalanceState(transactionBuilder).flatMap { balanceState: BalanceState ->
      if (balanceState == BalanceState.OK) {
        return@flatMap Single.just(true)
      } else {
        return@flatMap Single.just(false)
      }
    }
  }

  @Throws(UnknownServiceException::class)
  private fun map(
    transaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction,
    isBuyReady: Boolean
  ): CurrentPaymentStep {
    return when (transaction.status) {
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PENDING,
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PROCESSING -> when (transaction.gateway?.name) {
        Gateway.Name.appcoins -> CurrentPaymentStep.PAUSED_ON_CHAIN
        Gateway.Name.adyen_v2 -> if (transaction.status
          == com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PROCESSING
        ) {
          CurrentPaymentStep.PAUSED_CC_PAYMENT
        } else {
          if (isBuyReady) CurrentPaymentStep.READY else CurrentPaymentStep.NO_FUNDS
        }
        Gateway.Name.myappcoins -> CurrentPaymentStep.PAUSED_LOCAL_PAYMENT
        Gateway.Name.appcoins_credits -> CurrentPaymentStep.PAUSED_CREDITS
        Gateway.Name.unknown -> throw UnknownServiceException("Unknown gateway")
        else -> throw UnknownServiceException("Unknown gateway")
      }
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED, com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PENDING_USER_PAYMENT, com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.FAILED, com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.CANCELED, com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.INVALID_TRANSACTION -> if (isBuyReady) CurrentPaymentStep.READY else CurrentPaymentStep.NO_FUNDS
      else -> if (isBuyReady) CurrentPaymentStep.READY else CurrentPaymentStep.NO_FUNDS
    }
  }

  private fun resume(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String?,
    isBds: Boolean,
    type: String,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return if (isBds) {
      bdsResume(
        uri = uri,
        transactionType = transactionType,
        packageName = packageName,
        productName = productName,
        developerPayload = developerPayload,
        type = type,
        transactionBuilder = transactionBuilder
      )
    } else {
      Completable.error(java.lang.UnsupportedOperationException("Asf doesn't support resume."))
    }
  }

  private fun bdsResume(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String?,
    type: String,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return getTransaction(packageName, productName, type)
      .doOnSuccess { transaction -> saveTransactionId(transaction) }
      .flatMapCompletable { (uid): com.appcoins.wallet.bdsbilling.repository.entity.Transaction ->
        asfResume(
          uri = uri,
          transactionType = transactionType,
          packageName = packageName,
          productName = productName,
          approveKey = uid,
          developerPayload = developerPayload,
          transactionBuilder = transactionBuilder
        )
      }
  }

  private fun asfResume(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    approveKey: String,
    developerPayload: String?,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return if (transactionType == AsfInAppPurchaseInteractor.TransactionType.NORMAL) {
      buildPaymentTransaction(
        uri = uri,
        packageName = packageName,
        productName = productName,
        developerPayload = developerPayload!!,
        amount = transactionBuilder.amount()
      ).flatMapCompletable { paymentTransaction ->
        getSkuTransaction(
          paymentTransaction,
          packageName,
          approveKey
        )
      }
    } else Completable.error(
      java.lang.UnsupportedOperationException(
        "Transaction type $transactionType not supported"
      )
    )
  }

  private fun saveTransactionId(transaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction) {
    transactionFromApprove[transaction.uid] = transaction
  }

  private fun getSkuTransaction(
    paymentTransaction: PaymentTransaction,
    packageName: String,
    approveKey: String
  ): Completable {
    val billingType = valueOfInsensitive(paymentTransaction.transactionBuilder.type)
    return getSkuTransaction(
      merchantName = packageName,
      sku = paymentTransaction.transactionBuilder.skuId,
      scheduler = Schedulers.io(),
      type = billingType
    )
      .flatMapCompletable { transaction ->
        resumePayment(approveKey, paymentTransaction, transaction)
      }
  }

  private fun resumePayment(
    approveKey: String, paymentTransaction: PaymentTransaction,
    transaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction
  ): Completable {
    return when (transaction.status) {
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PENDING_SERVICE_AUTHORIZATION -> resume(
        paymentTransaction.uri,
        PaymentTransaction(paymentTransaction, PaymentState.APPROVED, approveKey)
      )
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PROCESSING -> trackTransaction(
        key = paymentTransaction.uri,
        packageName = paymentTransaction.packageName,
        skuId = paymentTransaction.transactionBuilder.skuId,
        uid = transaction.uid,
        purchaseUid = null,
        orderReference = transaction.orderReference
      )
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.PENDING,
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED,
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.INVALID_TRANSACTION,
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.FAILED,
      com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.CANCELED -> Completable.error(
        UnsupportedOperationException("Cannot resume from " + transaction.status + " state")
      )
      else -> Completable.error(
        UnsupportedOperationException("Cannot resume from " + transaction.status + " state")
      )
    }
  }

  private fun resume(key: String, paymentTransaction: PaymentTransaction): Completable {
    return checkFunds(key, paymentTransaction, buy(key, paymentTransaction), true)
  }

  private fun buy(key: String, paymentTransaction: PaymentTransaction): Completable {
    val transactionBuilder = paymentTransaction.transactionBuilder
    val cachedTransaction = getTransactionFromUid(key)
    val storeAddress: String = getStoreAddress(cachedTransaction)
    val oemAddress: String = getOemAddress(cachedTransaction)
    return Single.zip(
      countryCodeProvider.countryCode, getDefaultToken()
    ) { countryCode, tokenInfo ->
      transactionBuilder.appcoinsData(
        getBuyData(
          transactionBuilder = transactionBuilder,
          tokenInfo = tokenInfo,
          packageName = paymentTransaction.packageName,
          countryCode = countryCode,
          storeAddress = storeAddress,
          oemAddress = oemAddress
        )
      )
    }
      .map { transaction -> updateTransactionBuilderData(paymentTransaction, transaction) }
      .flatMap { payment: PaymentTransaction? ->
        bdsValidate(paymentTransaction)
          .map<PaymentTransaction> { payment }
      }
      .flatMapCompletable { payment: PaymentTransaction ->
        sendBuyTransaction(key, payment.transactionBuilder, true)
      }
  }

  private fun getTransactionFromUid(uid: String) = transactionFromApprove[uid]

  private fun getStoreAddress(
    transaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction?
  ): String {
    var tmpStoreAddress: String? = null
    if (transaction?.wallets != null) {
      tmpStoreAddress = transaction.wallets!!.store
    }
    return getStoreAddress(tmpStoreAddress)
  }

  private fun getStoreAddress(suggestedStoreAddress: String?): String {
    return suggestedStoreAddress?.let { suggestedStoreAddress } ?: defaultStoreAddress
  }

  private fun getOemAddress(
    transaction: com.appcoins.wallet.bdsbilling.repository.entity.Transaction?
  ): String {
    var tmpOemAddress: String? = null
    if (transaction?.wallets != null) {
      tmpOemAddress = transaction.wallets!!.oem
    }
    return getOemAddress(tmpOemAddress)
  }

  private fun getOemAddress(suggestedOemAddress: String?): String {
    return suggestedOemAddress?.let { suggestedOemAddress } ?: defaultOemAddress
  }

  private fun getBuyData(
    transactionBuilder: TransactionBuilder,
    tokenInfo: TokenInfo,
    packageName: String,
    countryCode: String,
    storeAddress: String,
    oemAddress: String
  ): ByteArray? {
    return TokenRepository.buyData(
      transactionBuilder.toAddress(),
      storeAddress,
      oemAddress,
      transactionBuilder.skuId,
      transactionBuilder.amount().multiply(BigDecimal("10").pow(transactionBuilder.decimals())),
      tokenInfo.address,
      packageName,
      convertCountryCode(countryCode)
    )
  }

  private fun updateTransactionBuilderData(
    paymentTransaction: PaymentTransaction,
    transaction: TransactionBuilder
  ): PaymentTransaction {
    return PaymentTransaction(
      paymentTransaction.uri, transaction,
      paymentTransaction.state, paymentTransaction.approveHash,
      paymentTransaction.buyHash, paymentTransaction.packageName,
      paymentTransaction.productName, paymentTransaction.productId,
      paymentTransaction.developerPayload, paymentTransaction.callbackUrl,
      paymentTransaction.orderReference, paymentTransaction.errorCode,
      paymentTransaction.errorMessage
    )
  }

  private fun trackTransaction(
    key: String,
    packageName: String,
    skuId: String,
    uid: String,
    purchaseUid: String?,
    orderReference: String?
  ): Completable {
    return bdsTrackTransactionCache.save(
      key,
      BdsTransaction(uid, purchaseUid, key, packageName, skuId, orderReference)
    )
  }

  private fun getCompletedPurchase(transaction: Payment, isBds: Boolean): Single<Payment> {
    return parseTransaction(transaction.uri, isBds)
      .flatMap { transactionBuilder ->
        if (isBds && transactionBuilder.type
            .equals(TransactionData.TransactionType.INAPP.name, ignoreCase = true)
        ) {
          return@flatMap getCompletedPurchase(
            packageName = transaction.packageName!!,
            productName = transaction.productId!!,
            purchaseUid = transaction.purchaseUid!!,
            type = transactionBuilder.type
          ).map { purchase -> mapToBdsPayment(transaction, purchase) }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { payment -> remove(transaction.uri).toSingleDefault(payment) }
        } else {
          return@flatMap Single.fromCallable { transaction }
            .flatMap { bundle -> remove(transaction.uri).toSingleDefault(bundle) }
        }
      }
  }

  private fun getCompletedPurchase(
    packageName: String,
    productName: String,
    purchaseUid: String,
    type: String
  ): Single<Purchase> {
    val billingType = valueOfManagedType(type)
    return getSkuTransaction(packageName, productName, Schedulers.io(), billingType)
      .flatMap { (_, status): com.appcoins.wallet.bdsbilling.repository.entity.Transaction ->
        if (status
          == com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status.COMPLETED
        ) {
          return@flatMap getSkuPurchase(
            merchantName = packageName,
            sku = productName,
            purchaseUid = purchaseUid,
            scheduler = Schedulers.io(),
            type = billingType
          )
        } else {
          return@flatMap Single.error<Purchase>(TransactionNotFoundException())
        }
      }
  }

  private fun getSkuPurchase(
    merchantName: String,
    sku: String,
    purchaseUid: String,
    scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<Purchase> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        getSkuPurchase(
          packageName = merchantName,
          skuId = sku,
          purchaseUid = purchaseUid,
          walletAddress = it.address,
          walletSignature = it.signedAddress,
          type = type
        )
      }
  }

  private fun getSkuPurchase(
    packageName: String,
    skuId: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Purchase> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      getSkuPurchase(
        packageName = packageName,
        skuId = skuId,
        walletAddress = walletAddress,
        walletSignature = walletSignature
      )
    } else {
      getSkuPurchaseSubs(
        packageName = packageName,
        purchaseUid = purchaseUid,
        walletAddress = walletAddress,
        walletSignature = walletSignature
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

  private fun map(packageName: String, inAppPurchaseResponse: InappPurchaseResponse): Purchase =
    Purchase(
      inAppPurchaseResponse.uid,
      RemoteProduct(inAppPurchaseResponse.sku),
      mapPurchaseState(inAppPurchaseResponse.state),
      false,
      null,
      Package(packageName),
      Signature(
        inAppPurchaseResponse.verification.signature,
        inAppPurchaseResponse.verification.data
      )
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
      subscriptionPurchaseResponse.uid,
      RemoteProduct(subscriptionPurchaseResponse.sku),
      mapPurchaseState(subscriptionPurchaseResponse.state),
      subscriptionPurchaseResponse.autoRenewing,
      mapRenewalDate(subscriptionPurchaseResponse.renewal),
      Package(packageName), Signature(
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

  private fun mapToBdsPayment(transaction: Payment, purchase: Purchase): Payment {
    return Payment(
      transaction.uri, transaction.status, purchase.uid,
      transaction.purchaseUid, purchase.signature
        .value, purchase.signature
        .message, transaction.orderReference, transaction.errorCode,
      transaction.errorMessage
    )
  }

  private fun remove(uri: String?): Completable {
    return asfRemove(uri, false)
      .andThen(bdsRemove(uri))
  }

  private fun bdsRemove(uri: String?): Completable {
    return asfRemove(uri, true)
  }

  private fun asfRemove(uri: String?, isBds: Boolean): Completable {
    return removeTransaction(uri, isBds)
      .andThen(removeTrackTransaction(uri))
  }

  private fun removeTransaction(key: String?, isBds: Boolean): Completable {
    return if (isBds) {
      bdsWatchedTransactionCache
    } else {
      asfWatchedTransactionCache
    }.remove(key)
      .andThen(
        if (isBds) {
          bdsBuyWatchedTransactionCache
        } else {
          asfBuyWatchedTransactionCache
        }.remove(key)
      )
      .andThen(
        if (isBds) {
          bdsPaymentTransactionCache
        } else {
          asfPaymentTransactionCache
        }.remove(key)
      )
  }

  private fun removeTrackTransaction(uri: String?): Completable {
    return bdsTrackTransactionCache.remove(uri)
  }

  private fun mapPurchase(
    purchaseId: String, signature: String, signatureData: String,
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

  private fun isWalletBlocked(): Single<Boolean> {
    return getWalletInfo(null, cached = false, updateFiat = false)
      .map { walletInfo -> walletInfo.blocked }
      .onErrorReturn { false }
      .delay(1, TimeUnit.SECONDS)
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

  private fun getCachedValidationStatus(walletAddress: String) =
    VerificationStatus.values()[pref.getInt(WALLET_VERIFIED + walletAddress, 4)]

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

  private fun getTransactionState(uri: String): Observable<Payment> {
    return Observable.merge(
      getAsfTransactionState(uri),
      getBdsTransactionState(uri)
    )
  }

  private fun getBdsTransactionState(uri: String): Observable<Payment?>? {
    return getAsfTransactionState(uri)
  }

  private fun getAsfTransactionState(uri: String): Observable<Payment?>? {
    return Observable.merge<Payment>(
      if (isBds) {
        bdsPaymentTransactionCache
      } else {
        asfPaymentTransactionCache
      }.get(uri).map { paymentTransaction -> mapToPayment(paymentTransaction) },
      bdsTrackTransactionCache.get(uri).map { transaction -> map(transaction) }
    )
  }

  private fun mapToPayment(paymentTransaction: PaymentTransaction): Payment {
    return Payment(
      paymentTransaction.uri,
      mapStatus(paymentTransaction.state),
      paymentTransaction.transactionBuilder.fromAddress(),
      paymentTransaction.buyHash,
      paymentTransaction.packageName,
      paymentTransaction.productName,
      paymentTransaction.productId,
      paymentTransaction.orderReference,
      paymentTransaction.errorCode,
      paymentTransaction.errorMessage
    )
  }

  private fun mapStatus(state: PaymentState?): Payment.Status {
    return when (state) {
      PaymentState.PENDING, PaymentState.APPROVING, PaymentState.APPROVED -> Payment.Status.APPROVING
      PaymentState.BUYING, PaymentState.BOUGHT -> Payment.Status.BUYING
      PaymentState.COMPLETED -> Payment.Status.COMPLETED
      PaymentState.ERROR -> Payment.Status.ERROR
      PaymentState.WRONG_NETWORK, PaymentState.UNKNOWN_TOKEN -> Payment.Status.NETWORK_ERROR
      PaymentState.NONCE_ERROR -> Payment.Status.NONCE_ERROR
      PaymentState.NO_TOKENS -> Payment.Status.NO_TOKENS
      PaymentState.NO_ETHER -> Payment.Status.NO_ETHER
      PaymentState.NO_FUNDS -> Payment.Status.NO_FUNDS
      PaymentState.NO_INTERNET -> Payment.Status.NO_INTERNET
      PaymentState.FORBIDDEN -> Payment.Status.FORBIDDEN
      PaymentState.SUB_ALREADY_OWNED -> Payment.Status.SUB_ALREADY_OWNED
      else -> throw IllegalStateException("State $state not mapped")
    }
  }

  private fun map(transaction: BdsTransaction?): Payment {
    return Payment(
      transaction?.key,
      mapStatus(transaction?.status),
      null,
      null,
      transaction?.packageName,
      null,
      transaction?.skuId,
      transaction?.orderReference,
      null,
      null
    )
  }

  private fun mapStatus(status: BdsTransaction.Status?): Payment.Status {
    return when (status) {
      BdsTransaction.Status.WAITING, BdsTransaction.Status.UNKNOWN_STATUS -> Payment.Status.ERROR
      BdsTransaction.Status.PROCESSING -> Payment.Status.BUYING
      BdsTransaction.Status.COMPLETED -> Payment.Status.COMPLETED
      else -> Payment.Status.ERROR
    }
  }

  companion object {
    private val TAG = _OnChainBuyLogic::class.java.simpleName
    private const val WALLET_VERIFIED = "wallet_verified_cc_"

    private val paymentGasLimit = BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT)

    private const val INSUFFICIENT_ERROR_MESSAGE = "insufficient funds for gas * price + value"
    private const val NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low"
    private const val FORBIDDEN_CODE = 403
  }
}

private enum class BalanceType {
  APPC, ETH, APPC_C
}

private enum class BalanceState {
  NO_TOKEN, NO_ETHER, NO_ETHER_NO_TOKEN, OK
}

data class ApproveTransaction(
  val key: String,
  val status: ApproveService.Status,
  val transactionHash: String?
)
