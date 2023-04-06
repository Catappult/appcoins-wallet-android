package com.asfoundation.wallet.change_currency.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.change_currency.ChangeFiatCurrency
import com.asfoundation.wallet.change_currency.FiatCurrencyEntity
import com.asfoundation.wallet.change_currency.list.model.FiatCurrencyModel_

class ChangeFiatCurrencyController : TypedEpoxyController<ChangeFiatCurrency>() {

  var clickListener: ((FiatCurrencyEntity) -> Unit)? = null

  override fun buildModels(model: ChangeFiatCurrency) {
    val selectedItem =
        model.list.find { fiatCurrency -> fiatCurrency.currency == model.selectedCurrency }

    selectedItem?.let { selected ->
      add(
          FiatCurrencyModel_()
              .id(selected.currency)
              .fiatCurrency(selected)
              .selected(true)
              .clickListener(clickListener)
      )
    }
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