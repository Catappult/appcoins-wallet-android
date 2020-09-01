package com.appcoins.wallet.gamification.repository.entity

import com.appcoins.wallet.gamification.Gamification.Companion.GAMIFICATION_ID
import com.appcoins.wallet.gamification.Gamification.Companion.REFERRAL_ID
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class PromotionsSerializer : JsonSerializer<PromotionsResponse> {

  override fun serialize(src: PromotionsResponse, typeOfSrc: Type?,
                         context: JsonSerializationContext): JsonElement {
    return when (src.id) {
      GAMIFICATION_ID -> context.serialize(src as GamificationResponse)
      REFERRAL_ID -> context.serialize(src as ReferralResponse)
      else -> context.serialize(src as GenericResponse)
    }
  }
}