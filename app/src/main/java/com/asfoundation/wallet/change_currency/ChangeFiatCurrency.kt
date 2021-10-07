package com.asfoundation.wallet.change_currency

data class ChangeFiatCurrency(val list: List<FiatCurrencyEntity>, val selectedCurrency: String)