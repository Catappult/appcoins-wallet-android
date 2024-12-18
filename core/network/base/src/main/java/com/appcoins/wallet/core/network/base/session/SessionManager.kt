package com.appcoins.wallet.core.network.base.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

  private var accessToken: String? = null
  private var accessTokenExpirationTime: Long? = null

  fun isAccessTokenExpired(): Boolean {
    val currentTimeMillis = System.currentTimeMillis() / 1000L
    return accessTokenExpirationTime?.let { currentTimeMillis >= it } ?: true
  }

  fun updateAccessToken(token: String, expiresIn: Long) {
    accessToken = token
    accessTokenExpirationTime = expiresIn
  }

  fun getAccessToken() = accessToken
}
