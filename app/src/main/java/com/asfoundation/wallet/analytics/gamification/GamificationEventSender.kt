package com.asfoundation.wallet.analytics.gamification

interface GamificationEventSender {

  fun sendMainScreenViewEvent(userLevel: Int)

  fun sendMoreInfoScreenViewEvent(userLevel: Int)
}