package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import com.appcoins.wallet.billing.carrierbilling.*
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.StringProvider
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.applicationinfo.ApplicationInfoLoader
import com.asfoundation.wallet.util.safeLet
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.math.BigDecimal
import java.util.*

class CarrierVerifyPresenter(
    private val disposables: CompositeDisposable,
    private val view: CarrierVerifyView,
    private val data: CarrierVerifyData,
    private val navigator: CarrierVerifyNavigator,
    private val interactor: CarrierInteractor,
    private val billingAnalytics: BillingAnalytics,
    private val appInfoLoader: ApplicationInfoLoader,
    private val stringProvider: StringProvider,
    private val formatter: CurrencyFormatUtils,
    private val logger: Logger,
    private val viewScheduler: Scheduler,
    private val ioScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleBackButton()
    handleNextButton()
    handleOtherPaymentsButton()
    handlePhoneNumberChange()
  }

  private fun initializeView() {
    disposables.add(
        appInfoLoader.getApplicationInfo(data.domain)
            .observeOn(viewScheduler)
            .doOnSuccess { ai ->
              view.initializeView(ai.appName, ai.icon, data.currency, data.fiatAmount,
                  data.appcAmount, data.skuDescription, data.bonusAmount, data.preselected)
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
              billingAnalytics.sendPaymentMethodDetailsActionEvent(data.domain,
                  data.skuId, data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
                  data.transactionType,
                  "next")
            }
            .flatMapSingle { phoneNumber ->
              interactor.createPayment(phoneNumber, data.domain, data.origin, data.transactionData,
                  data.transactionType, data.currency, data.fiatAmount.toString())
            }
            .observeOn(viewScheduler)
            .flatMap { paymentModel ->
              view.unlockRotation()
              var completable = Completable.complete()
              if (paymentModel.error !is NoError) {
                completable = handleError(paymentModel)
              } else {
                if (paymentModel.status == TransactionStatus.PENDING_USER_PAYMENT) {
                  completable = Completable.fromAction {
                    safeLet(paymentModel.carrier, paymentModel.fee) { carrier, fee ->
                      fee.cost?.let { cost ->
                        navigator.navigateToFee(paymentModel.uid, data.domain,
                            data.transactionData, data.transactionType, paymentModel.paymentUrl!!,
                            data.currency, data.fiatAmount, data.appcAmount, data.bonusAmount,
                            data.skuDescription, data.skuId, cost.value, carrier.name, carrier.icon)
                      }
                    }
                  }
                }
              }
              return@flatMap completable.andThen(Observable.just(paymentModel))
            }
            .onErrorResumeNext { e: Throwable -> handleException(e) }
            .retry()
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleException(throwable: Throwable): Observable<CarrierPaymentModel> {
    logger.log(CarrierVerifyFragment.TAG, throwable)
    return if (throwable is HttpException) {
      if (throwable.code() == 403) {
        handleFraudFlow()
            .andThen(Observable.just(
                CarrierPaymentModel(GenericError(true, throwable.code(), throwable.message()))))
      } else {
        Completable.fromAction {
          navigator.navigateToError(stringProvider.getString(R.string.activity_iab_error_message))
        }
            .andThen(Observable.just(
                CarrierPaymentModel(GenericError(true, throwable.code(), throwable.message()))))

      }
    } else {
      Completable.fromAction {
        navigator.navigateToError(stringProvider.getString(R.string.activity_iab_error_message))
      }
          .andThen(Observable.just(CarrierPaymentModel(GenericError(false, -1, throwable.message))))
          .subscribeOn(viewScheduler)
    }
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
      is GenericError -> {
        if (paymentModel.error.errorCode == 403) {
          return handleFraudFlow()
        }
        return Completable.fromAction {
          navigator.navigateToError(
              stringProvider.getString(R.string.activity_iab_error_message))
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
              navigator.navigateToWalletValidation(R.string.purchase_error_wallet_block_code_403)
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
                billingAnalytics.sendPaymentMethodDetailsActionEvent(data.domain, data.skuId,
                    data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
                    data.transactionType, "cancel")
                navigator.finishActivityWithError()
              } else {
                billingAnalytics.sendPaymentMethodDetailsActionEvent(data.domain, data.skuId,
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
              billingAnalytics.sendPaymentMethodDetailsActionEvent(data.domain,
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