package com.appcoins.wallet.gamification.repository.entity

import com.appcoins.wallet.gamification.Gamification.Companion.GAMIFICATION_ID
import com.appcoins.wallet.gamification.Gamification.Companion.REFERRAL_ID
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type


class PromotionsDeserializer : JsonDeserializer<PromotionsResponse> {

  override fun deserialize(json: JsonElement, member: Type?,
                           context: JsonDeserializationContext): PromotionsResponse {
    val id = json.asJsonObject
        .get("id")
        .asString
    return when (id) {
      GAMIFICATION_ID -> context.deserialize(json, GamificationResponse::class.java)
      REFERRAL_ID -> context.deserialize(json, ReferralResponse::class.java)
      else -> context.deserialize(json, GenericResponse::class.java)
    }
  }
}