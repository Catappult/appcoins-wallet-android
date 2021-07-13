package com.asfoundation.wallet.change_currency.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.change_currency.ChangeFiatCurrency
import com.asfoundation.wallet.change_currency.FiatCurrency
import com.asfoundation.wallet.change_currency.list.model.FiatCurrencyModel_

class ChangeFiatCurrencyController : TypedEpoxyController<ChangeFiatCurrency>() {

  var clickListener: ((FiatCurrency) -> Unit)? = null

  override fun buildModels(model: ChangeFiatCurrency) {
    val selectedItem =
        model.list.find { fiatCurrency -> fiatCurrency.currency == model.selectedCurrency }!!

    add(
        FiatCurrencyModel_()
            .id(selectedItem.currency)
            .fiatCurrency(selectedItem)
            .selected(true)
            .clickListener(clickListener)
    )
    for (fiatCurrency in model.list) {
      if (selectedItem != fiatCurrency) {
        add(
            FiatCurrencyModel_()
                .id(fiatCurrency.currency)
                .fiatCurrency(fiatCurrency)
                .selected(false)
                .clickListener(clickListener)
        )
      }
    }
  }
}