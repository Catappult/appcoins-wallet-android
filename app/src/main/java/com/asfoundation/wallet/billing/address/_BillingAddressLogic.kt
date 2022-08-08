package com.asfoundation.wallet.billing.address

import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.ui._Back
import com.asfoundation.wallet.ui._BillingAddress2ViewState
import com.asfoundation.wallet.ui._Navigator
import com.asfoundation.wallet.ui._View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class _BillingAddressLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val data: BillingAddressData,
  private val billingAddressRepository: BillingAddressRepository,
  private val billingAnalytics: BillingAnalytics
) {

  fun present() = initializeView()

  private fun initializeView() {
    view.setState(
      _BillingAddress2ViewState(
        data.bonus,
        data.isDonation,
        data.domain,
        data.skuDescription,
        data.appcAmount,
        data.fiatAmount,
        data.fiatCurrency,
        data.isStored,
        data.shouldStoreCard,
        billingAddressRepository.retrieveBillingAddress()
      )
    )
  }

  fun onSubmitClicks(billingAddressModel: BillingAddressModel) {
    Observable.just(billingAddressModel)
      .subscribeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        val billingModel =
          BillingAddressModel(
            address = it.address,
            city = it.city,
            zipcode = it.zipcode,
            state = it.state,
            country = it.country,
            number = it.number,
            remember = data.shouldStoreCard
          )
        if (data.shouldStoreCard) {
          billingAddressRepository.saveBillingAddress(billingModel)
        }
        sendActionEventAnalytics(if (data.isDonation) "donate" else "buy")
        navigator.navigate(_Back)
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun onBackClicks() {
    sendActionEventAnalytics("back")
    navigator.navigate(_Back)
  }

  private fun sendActionEventAnalytics(action: String) {
    billingAnalytics.sendBillingAddressActionEvent(
      data.domain,
      data.skuId,
      data.appcAmount.toString(),
      BillingAnalytics.PAYMENT_METHOD_CC,
      data.transactionType,
      action
    )
  }

}