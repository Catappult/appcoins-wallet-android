package com.appcoins.wallet.gamification.repository.entity

enum class GamificationStatus {
  NONE,
  STANDARD,
  APPROACHING_NEXT_LEVEL,
  APPROACHING_VIP,
  VIP,
  APPROACHING_VIP_MAX,
  VIP_MAX;

  companion object {
    fun toEnum(value: String?) = values().firstOrNull {it.name == value} ?: NONE
  }
}

