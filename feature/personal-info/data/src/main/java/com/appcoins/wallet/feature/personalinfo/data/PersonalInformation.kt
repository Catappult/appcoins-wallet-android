package com.appcoins.wallet.feature.personalinfo.data

data class PersonalInformation(
  var country: CountriesModel = CountriesModel("", ""),
  var address: String = "",
  var name: String = "",
  var city: String = "",
  var zipCode: String = "",
  var email: String = "",
  var fiscalId: String = "",
)