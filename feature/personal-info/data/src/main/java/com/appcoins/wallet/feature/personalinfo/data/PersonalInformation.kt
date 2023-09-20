package com.appcoins.wallet.feature.personalinfo.data

import com.appcoins.wallet.core.network.backend.model.PersonalInformationRequest

data class PersonalInformation(
  var country: CountriesModel = CountriesModel("", ""),
  var address: String = "",
  var name: String = "",
  var city: String = "",
  var zipCode: String = "",
  var email: String = "",
  var fiscalId: String = "",
) {

  fun mapToRequest() = PersonalInformationRequest(
    name = name.ifBlank { null },
    email = email,
    address = address,
    city = city,
    postalCode = zipCode,
    country = country.name,
    fiscalId = fiscalId
  )
}

fun PersonalInformationRequest.mapToModel() = PersonalInformation(
  country = CountriesModel("", ""),
  address = address ?: "",
  name = name ?: "",
  city = city ?: "",
  zipCode = postalCode ?: "",
  email = email ?: "",
  fiscalId = fiscalId ?: ""
)