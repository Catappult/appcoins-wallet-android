package com.appcoins.wallet.gamification.repository.entity

data class UserStatusResponse(val gamification: UserStatsGamification,
                              val referral: UserStatsReferral?)
