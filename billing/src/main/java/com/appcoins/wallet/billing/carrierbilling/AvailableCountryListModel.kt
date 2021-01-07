package com.appcoins.wallet.billing.carrierbilling

data class AvailableCountryListModel(val countryList: List<String>,
                                     val hasError: Boolean = false) {
  constructor() : this(emptyList(), true)
}
