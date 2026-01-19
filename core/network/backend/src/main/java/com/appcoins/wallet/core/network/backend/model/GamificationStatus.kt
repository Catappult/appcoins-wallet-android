package com.appcoins.wallet.core.network.backend.model

enum class GamificationStatus {
  NONE,
  STANDARD,
  APPROACHING_NEXT_LEVEL,
  APPROACHING_VIP,
  VIP,
  APPROACHING_VIP_MAX,
  VIP_MAX;

  companion object {
    fun toEnum(value: String?) = GamificationStatus.entries.firstOrNull { it.name == value } ?: NONE
  }

  fun isVip(): Boolean {
    return this == VIP || this == VIP_MAX
  }
}

