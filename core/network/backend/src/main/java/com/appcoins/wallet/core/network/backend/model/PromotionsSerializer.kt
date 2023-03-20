package com.appcoins.wallet.core.network.backend.model

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class PromotionsSerializer : JsonSerializer<PromotionsResponse> {

  override fun serialize(
    src: PromotionsResponse, typeOfSrc: Type?,
    context: JsonSerializationContext
  ): JsonElement {
    return when (src.id) {
      GAMIFICATION_ID -> context.serialize(src as GamificationResponse)
      REFERRAL_ID -> context.serialize(src as ReferralResponse)
      else -> context.serialize(src as GenericResponse)
    }
  }

  companion object {
    const val GAMIFICATION_ID = "GAMIFICATION"
    const val REFERRAL_ID = "REFERRAL"
  }
}