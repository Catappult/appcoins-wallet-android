package com.appcoins.wallet.core.analytics.analytics.gamification

interface GamificationEventSender {

  fun sendMainScreenViewEvent(userLevel: Int)

  fun sendMoreInfoScreenViewEvent(userLevel: Int)
}