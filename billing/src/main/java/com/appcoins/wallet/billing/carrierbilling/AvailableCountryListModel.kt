package com.appcoins.wallet.billing.carrierbilling

import java.util.*

data class AvailableCountryListModel(val countryList: List<String>,
                                     val defaultCountry: String?,
                                     val hasError: Boolean = false) {
  constructor() : this(emptyList(), null, true)

  fun convertListToString(): String {
    return countryList.joinToString(",") { country -> country.toLowerCase(Locale.ROOT) }
  }

  fun shouldFilter(): Boolean {
    return !hasError && countryList.isNotEmpty()
  }
}
