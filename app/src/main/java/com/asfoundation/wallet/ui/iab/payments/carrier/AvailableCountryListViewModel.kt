package com.asfoundation.wallet.ui.iab.payments.carrier

data class AvailableCountryListViewModel(val countryList: List<String>, //Non-empty list
                                         val countryListString: String, //List of countries separated by commans
                                         val shouldFilter: Boolean) {

  constructor(shouldFilter: Boolean) : this(emptyList(), "", shouldFilter)
}