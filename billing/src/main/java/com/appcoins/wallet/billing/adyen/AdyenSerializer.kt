package com.appcoins.wallet.billing.adyen

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.model.payments.response.Threeds2Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.model.payments.response.Threeds2FingerprintAction
import com.google.gson.JsonObject
import org.json.JSONObject
import javax.inject.Inject

open class AdyenSerializer @Inject constructor() {

  open fun deserializePaymentMethods(response: PaymentMethodsResponse): PaymentMethodsApiResponse {
    return PaymentMethodsApiResponse.SERIALIZER.deserialize(JSONObject(response.payment.toString()))
  }

  open fun deserializeRedirectAction(jsonAction: JsonObject): RedirectAction {
    return RedirectAction.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
  }

  open fun deserialize3DSFingerprint(jsonAction: JsonObject): Threeds2FingerprintAction {
    return Threeds2FingerprintAction.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
  }

  open fun deserialize3DSChallenge(jsonAction: JsonObject): Threeds2ChallengeAction {
    return Threeds2ChallengeAction.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
  }

  open fun deserialize3DS(jsonAction: JsonObject): Threeds2Action =
    Threeds2Action.SERIALIZER.deserialize(JSONObject(jsonAction.toString()))
}
