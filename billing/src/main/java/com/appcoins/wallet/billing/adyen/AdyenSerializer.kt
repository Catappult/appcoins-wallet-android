package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.response.RedirectAction
import com.adyen.checkout.base.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.base.model.payments.response.Threeds2FingerprintAction
import com.google.gson.JsonObject
import org.json.JSONObject

class AdyenSerializer {

  fun deserializePaymentMethods(response: PaymentMethodsResponse): PaymentMethodsApiResponse {
    return PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(response.payment.toString()))
  }

  fun deserializeRedirectAction(jsonAction: JsonObject): RedirectAction {
    return RedirectAction.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
  }

  fun deserialize3DSFingerprint(jsonAction: JsonObject): Threeds2FingerprintAction {
    return Threeds2FingerprintAction.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
  }

  fun deserialize3DSChallenge(jsonAction: JsonObject): Threeds2ChallengeAction {
    return Threeds2ChallengeAction.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
  }
}
