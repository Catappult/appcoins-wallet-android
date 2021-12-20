package com.asfoundation.wallet.skills

import cm.aptoide.skills.interfaces.ExternalSkillsPaymentProvider
import cm.aptoide.skills.model.CreatedTicket
import cm.aptoide.skills.model.Price
import cm.aptoide.skills.util.EskillsPaymentData
import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.repository.CurrencyConversionService
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class SkillsPaymentRepository(
    private val currencyConversionService: CurrencyConversionService,
    private val currencyFormatUtils: CurrencyFormatUtils,
    private val appCoinsCreditsPayment: AppCoinsCreditsPayment,
    private val schedulers: RxSchedulers,
) : ExternalSkillsPaymentProvider {
  override fun getLocalFiatAmount(value: BigDecimal, currency: String): Single<Price> {
    return currencyConversionService.getLocalFiatAmount(value.toString(), currency)
        .map { Price(it.amount, it.currency, it.symbol) }
        .subscribeOn(schedulers.io)
  }

  override fun getFiatToAppcAmount(value: BigDecimal, currency: String): Single<Price> {
    return currencyConversionService.getFiatToAppcAmount(value.toString(), currency)
        .map { Price(it.amount, it.currency, it.symbol) }
        .subscribeOn(schedulers.io)
  }

  override fun getFormattedAppcAmount(value: BigDecimal, currency: String): Single<String> {
    return getFiatToAppcAmount(value, currency)
        .map { currencyFormatUtils.formatCurrency(it.amount, WalletCurrency.APPCOINS) }
  }

  override fun pay(eskillsPaymentData: EskillsPaymentData, ticket: CreatedTicket): Completable {
    return appCoinsCreditsPayment.pay(eskillsPaymentData, ticket)
        .subscribeOn(schedulers.io)
  }
}
