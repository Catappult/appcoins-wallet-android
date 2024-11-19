package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import com.asfoundation.wallet.di.IoDispatcher
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineDispatcher
import org.json.JSONObject
import javax.inject.Inject

class SubmitRedirectUseCase @Inject constructor(
  private val adyenPaymentInteractor: AdyenPaymentInteractor,
  @IoDispatcher private val networkDispatcher: CoroutineDispatcher,
) {
  suspend operator fun invoke(
    uid: String,
    details: JSONObject?,
    paymentData: String?
  ) =
    adyenPaymentInteractor.submitRedirect(
      uid = uid,
      details = details?.run { JsonParser.parseString(toString()).asJsonObject } ?: JsonObject(),
      paymentData = paymentData,
    ).callAsync(networkDispatcher)
}