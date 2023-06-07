package com.appcoins.wallet.feature.promocode.data.repository

import java.io.Serializable

data class PromoCode(
    val code: String?,
    val bonus: Double?,
    val validity: ValidityState?,
    val appName: String?,
) : Serializable

enum class ValidityState(val value: Int) {
  ACTIVE(0),
  EXPIRED(1),
  ERROR(2),
  NOT_ADDED(3);

  companion object {
    fun toEnum(value: Int) = ValidityState.values().firstOrNull { it.value == value }
  }
}
