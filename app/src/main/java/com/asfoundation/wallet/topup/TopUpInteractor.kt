package com.asfoundation.wallet.topup

import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.core.network.microservices.model.FeeEntity
import com.appcoins.wallet.core.network.microservices.model.FeeType
import com.appcoins.wallet.core.network.microservices.model.PaymentMethodEntity
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.currencies.LocalCurrencyConversionService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asfoundation.wallet.backup.NotificationNeeded
import com.asfoundation.wallet.billing.googlepay.usecases.FilterValidGooglePayUseCase
import com.asfoundation.wallet.billing.paypal.PaypalSupportedCurrencies
import com.asfoundation.wallet.billing.true_layer.usecases.AddTrueLayerPaymentMessageUseCase
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodFee
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal
import javax.inject.Inject

class TopUpInteractor @Inject constructor(
  private val repository: BdsRepository,
  private val conversionService: LocalCurrencyConversionService,
  private val gamificationInteractor: GamificationInteractor,
  private val topUpValuesService: TopUpValuesService,
  private var walletBlockedInteract: WalletBlockedInteract,
  private var inAppPurchaseInteractor: InAppPurchaseInteractor,
  private var supportInteractor: SupportInteractor,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val filterValidGooglePayUseCase: FilterValidGooglePayUseCase,
  private val addTrueLayerPaymentMessageUseCase: AddTrueLayerPaymentMessageUseCase,
  private val walletService: WalletService,
) {

  private var chipValuesIndexMap: MutableList<LinkedHashMap<FiatValue, Int>> = mutableListOf()
  private var limitValues: HashMap<String, TopUpLimitValues> = hashMapOf()

  fun getPaymentMethods(
    value: String,
    currency: String
  ): Single<List<PaymentMethod>> =
    walletService.getWalletAddress()
      .flatMap { walletAddress ->
        repository.getPaymentMethods(
          value = value,
          currency = currency,
          currencyType = "fiat",
          direct = true,
          transactionType = "TOPUP",
          entityOemId = null,
          address = walletAddress
        )
      }
      .map { repository.replaceAppcPricesToOriginalPrices(it, value, currency) }
      .map { mapPaymentMethods(it, currency) }
      .map { filterValidGooglePayUseCase(it) }
      .map { addTrueLayerPaymentMessageUseCase(it) }

  fun isWalletBlocked() = walletBlockedInteract.isWalletBlocked()

  fun incrementAndValidateNotificationNeeded(): Single<NotificationNeeded> =
    inAppPurchaseInteractor.incrementAndValidateNotificationNeeded()

  fun showSupport(): Completable = supportInteractor.showSupport()

  fun convertAppc(value: String): Single<FiatValue> = conversionService.getAppcToLocalFiat(value, 2)

  fun convertLocal(currency: String, value: String, scale: Int): Single<FiatValue> =
    conversionService.getFiatToAppc(currency, value, scale)

  private fun mapPaymentMethods(
    paymentMethods: List<PaymentMethodEntity>, currency: String
  ): List<PaymentMethod> = paymentMethods.map {
    PaymentMethod(
      id = it.id,
      label = it.label,
      iconUrl = it.iconUrl,
      async = it.async,
      fee = mapPaymentMethodFee(it.fee),
      isEnabled = it.isAvailable(),
      disabledReason = null,
      showLogout = isToShowPaypalLogout(it) || isToShowAmazonPayLogout(it),
      showExtraFeesMessage = hasExtraFees(it, currency),
      price = FiatValue(it.price.value, it.price.currency)
    )
  }

  private fun isToShowPaypalLogout(paymentMethod: PaymentMethodEntity): Boolean {
    return (paymentMethod.id == PaymentMethodsView.PaymentMethodId.PAYPAL_V2.id)
  }

  private fun isToShowAmazonPayLogout(paymentMethod: PaymentMethodEntity): Boolean {
    return (paymentMethod.id == PaymentMethodsView.PaymentMethodId.AMAZONPAY.id)
  }

  private fun hasExtraFees(paymentMethod: PaymentMethodEntity, currency: String): Boolean {
    return (paymentMethod.id == PaymentMethodsView.PaymentMethodId.PAYPAL_V2.id && !PaypalSupportedCurrencies.currencies.contains(
      currency
    ))
  }

  private fun mapPaymentMethodFee(feeEntity: FeeEntity?): PaymentMethodFee? = feeEntity?.let {
    if (feeEntity.type === FeeType.EXACT) {
      PaymentMethodFee(true, feeEntity.cost?.value, feeEntity.cost?.currency)
    } else {
      PaymentMethodFee(false, null, null)
    }
  }

  fun getEarningBonus(
    packageName: String,
    amount: BigDecimal,
    currency: String,
    paymentMethodId: String?
  ): Single<ForecastBonusAndLevel> =
    getCurrentPromoCodeUseCase()
      .flatMap {
        gamificationInteractor.getEarningBonus(
          packageName = packageName,
          amount = amount,
          promoCodeString = it.code,
          currency = currency,
          paymentMethodId = paymentMethodId,
        )
      }

  fun getLimitTopUpValues(
    currency: String? = null,
    method: String? = null
  ): Single<TopUpLimitValues> {
    val limitValue = method?.let { limitValues[it] }

    if (limitValue != null) {
      return Single.just(limitValue)
    }

    return topUpValuesService.getLimitValues(
      currency = currency,
      method = method
    )
      .doOnSuccess {
        if (!it.error.hasError && method != null)
          cacheLimitValues(method, it)
      }
  }

  fun getDefaultValues(currency: String? = null): Single<TopUpValuesModel> {
    val chipValueIndexMap = chipValuesIndexMap.firstOrNull { linkedHashMap ->
      linkedHashMap.keys.any { it.currency == currency }
    } ?: LinkedHashMap()
    return if (chipValueIndexMap.isNotEmpty()) {
      Single.just(TopUpValuesModel(ArrayList(chipValueIndexMap.keys)))
    } else {
      topUpValuesService.getDefaultValues(currency = currency)
        .doOnSuccess { if (!it.error.hasError) cacheChipValues(it.values) }
    }
  }

  fun cleanCachedValues() {
    limitValues.clear()
    chipValuesIndexMap.clear()
  }

  fun isBonusValidAndActive(): Boolean = gamificationInteractor.isBonusActiveAndValid()

  fun isBonusValidAndActive(forecastBonusAndLevel: ForecastBonusAndLevel): Boolean =
    gamificationInteractor.isBonusActiveAndValid(forecastBonusAndLevel)

  private fun cacheChipValues(chipValues: List<FiatValue>) {
    chipValuesIndexMap.forEach { chipValueIndexMap ->
      for (index in chipValues.indices) {
        chipValueIndexMap[chipValues[index]] = index
      }
    }
  }

  private fun cacheLimitValues(method: String, values: TopUpLimitValues) {
    limitValues[method] = values
  }

  fun getWalletAddress(): Single<String> = inAppPurchaseInteractor.walletAddress

}
