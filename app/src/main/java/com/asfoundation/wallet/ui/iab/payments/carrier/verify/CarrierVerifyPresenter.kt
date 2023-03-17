package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.appcoins.wallet.billing.carrierbilling.*
import com.appcoins.wallet.billing.carrierbilling.ForbiddenError.ForbiddenType
import com.appcoins.wallet.core.network.microservices.model.TransactionStatus
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.appcoins.wallet.commons.Logger
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.asfoundation.wallet.util.StringProvider
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.appcoins.wallet.core.utils.common.applicationinfo.ApplicationInfoProvider
import com.asfoundation.wallet.util.safeLet
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.*

class CarrierVerifyPresenter(
  private val disposables: CompositeDisposable,
  private val view: CarrierVerifyView,
  private val data: CarrierVerifyData,
  private val navigator: CarrierVerifyNavigator,
  private val interactor: CarrierInteractor,
  private val billingAnalytics: BillingAnalytics,
  private val appInfoProvider: ApplicationInfoProvider,
  private val stringProvider: StringProvider,
  private val formatter: CurrencyFormatUtils,
  private val logger: Logger,
  private val networkScheduler: Scheduler,
  private val viewScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleAvailableCountryList()
    handleBackButton()
    handleNextButton()
    handleOtherPaymentsButton()
    handlePhoneNumberChange()
    handleChangeButtonClick()
  }

  private fun handleAvailableCountryList() {
    disposables.add(interactor.retrieveAvailableCountries()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.shouldFilter()) {
            view.filterCountries(it.convertListToString(), it.defaultCountry)
          }
          val phoneNumber = interactor.retrievePhoneNumber()
          if (phoneNumber != null) view.showSavedPhoneNumber(phoneNumber)
          else view.hideSavedPhoneNumber()
          view.showPhoneNumberLayout()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun initializeView() {
    view.initializeView(data.currency, data.fiatAmount, data.appcAmount, data.skuDescription,
        data.bonusAmount, data.preselected)
    disposables.add(
        appInfoProvider.getApplicationInfo(data.domain)
            .observeOn(viewScheduler)
            .doOnSuccess { ai ->
              view.setAppDetails(ai.appName, ai.icon)
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleChangeButtonClick() {
    disposables.add(
        view.changeButtonClick()
            .observeOn(viewScheduler)
            .doOnNext { _ ->
              interactor.forgetPhoneNumber()
              view.hideSavedPhoneNumber(true)
              view.focusOnPhoneNumber()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handlePhoneNumberChange() {
    disposables.add(
        view.phoneNumberChangeEvent()
            .doOnNext { event ->
              view.setNextButtonEnabled(event.second)
              view.removePhoneNumberFieldError()
            }
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleNextButton() {
    disposables.add(
        view.nextClickEvent()
            .doOnNext {
              view.lockRotation()
              view.setLoading()
              billingAnalytics.sendActionPaymentMethodDetailsActionEvent(data.domain,
                  data.skuId, data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
                  data.transactionType,
                  "next")
            }
            .flatMap { phoneNumber ->
              interactor.createPayment(phoneNumber, data.domain, data.origin, data.transactionData,
                  data.transactionType, data.currency, data.fiatAmount.toString())
                  .observeOn(viewScheduler)
                  .flatMapObservable { paymentModel ->
                    view.unlockRotation()
                    var completable = Completable.complete()
                    if (paymentModel.error !is NoError) {
                      completable = handleError(paymentModel)
                    } else if (paymentModel.status == TransactionStatus.PENDING_USER_PAYMENT) {
                      completable = handleUnknownFeeOrCarrier()
                      safeLet(paymentModel.carrier, paymentModel.fee) { carrier, fee ->
                        fee.cost?.let { cost ->
                          completable = Completable.fromAction {
                            navigator.navigateToFee(paymentModel.uid, data.domain,
                                data.transactionData, data.transactionType,
                                paymentModel.paymentUrl!!, data.currency, data.fiatAmount,
                                data.appcAmount, data.bonusAmount, data.skuDescription,
                                data.skuId, cost.value, carrier.name, carrier.icon, phoneNumber)
                          }
                        }
                      }
                    }
                    return@flatMapObservable completable.andThen(Observable.just(paymentModel))
                  }
            }
            .observeOn(viewScheduler)
            .doOnError { handleException(it) }
            .retry()
            .subscribe({}, { handleException(it) })
    )
  }

  private fun handleUnknownFeeOrCarrier(): Completable {
    return Completable.fromAction {
      logger.log(CarrierVerifyFragment.TAG, "Unknown fee or carrier")
      navigator.navigateToError(stringProvider.getString(R.string.activity_iab_error_message))
    }
  }

  private fun handleException(throwable: Throwable) {
    logger.log(CarrierVerifyFragment.TAG, throwable)
    navigator.navigateToError(stringProvider.getString(R.string.activity_iab_error_message))
  }

  private fun handleError(paymentModel: CarrierPaymentModel): Completable {
    logger.log(CarrierVerifyFragment.TAG, paymentModel.error.errorMessage)
    when (paymentModel.error) {
      is InvalidPhoneNumber -> {
        return Completable.fromAction { view.showInvalidPhoneNumberError() }
      }
      is InvalidPriceError -> {
        val error = paymentModel.error as InvalidPriceError
        when (error.type) {
          InvalidPriceError.BoundType.LOWER -> {
            return Completable.fromAction {
              navigator.navigateToError(
                  stringProvider.getString(R.string.purchase_carrier_error_minimum,
                      formatFiatValue(error.value, data.currency)))
            }
          }
          InvalidPriceError.BoundType.UPPER -> {
            return Completable.fromAction {
              navigator.navigateToError(
                  stringProvider.getString(R.string.purchase_carrier_error_maximum,
                      formatFiatValue(error.value, data.currency)))
            }
          }
        }
      }
      is ForbiddenError -> {
        val error = paymentModel.error as ForbiddenError
        return if (error.type == ForbiddenType.BLOCKED) handleFraudFlow()
        else Completable.fromAction {
          navigator.navigateToError(
              stringProvider.getString(R.string.subscriptions_error_already_subscribed))
        }
      }
      is GenericError -> {
        return Completable.fromAction {
          navigator.navigateToError(stringProvider.getString(R.string.activity_iab_error_message))
        }
      }
      else -> return Completable.complete()
    }
  }

  private fun handleFraudFlow(): Completable {
    return interactor.getWalletStatus()
        .observeOn(viewScheduler)
        .doOnSuccess { walletStatus ->
          if (walletStatus.blocked) {
            if (walletStatus.verified) {
              navigator.navigateToError(
                  stringProvider.getString(R.string.purchase_error_wallet_block_code_403))
            } else {
              navigator.navigateToVerification()
            }
          } else {
            navigator.navigateToError(
                stringProvider.getString(R.string.purchase_error_wallet_block_code_403))
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


  private fun handleBackButton() {
    disposables.add(
        view.backEvent()
            .doOnNext {
              if (data.preselected) {
                billingAnalytics.sendActionPaymentMethodDetailsActionEvent(data.domain, data.skuId,
                    data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
                    data.transactionType, "cancel")
                navigator.finishActivityWithError()
              } else {
                billingAnalytics.sendActionPaymentMethodDetailsActionEvent(data.domain, data.skuId,
                    data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
                    data.transactionType,
                    "back")
                navigator.navigateBack()
              }
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  private fun handleOtherPaymentsButton() {
    disposables.add(
        view.otherPaymentMethodsEvent()
            .doOnNext {
              billingAnalytics.sendActionPaymentMethodDetailsActionEvent(data.domain,
                  data.skuId, data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
                  data.transactionType,
                  "other_payments")
              interactor.removePreSelectedPaymentMethod()
              navigator.navigateBack()
            }
            .retry()
            .subscribe({}, { e -> e.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()

}