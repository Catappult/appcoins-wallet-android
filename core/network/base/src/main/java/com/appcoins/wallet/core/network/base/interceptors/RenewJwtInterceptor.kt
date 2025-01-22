package com.appcoins.wallet.core.network.base.interceptors

import android.util.Log
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.base.WalletRepository
import com.appcoins.wallet.core.network.base.compat.RenewJwtApi
import com.appcoins.wallet.core.network.base.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class RenewJwtInterceptor @Inject constructor(
  private val ewtAuthenticatorService: EwtAuthenticatorService,
  private val renewJwtApi: RenewJwtApi,
  private val sessionManager: SessionManager,
  private val walletRepository: WalletRepository
) : Interceptor {

  private companion object {
    const val MAX_RETRIES = 5
    const val UNAUTHORIZED = 401
  }

  private val lock = ReentrantLock(true)

  override fun intercept(chain: Interceptor.Chain): Response {
    // in some endpoints, when need to use a specific wallet and not the default one. In these cases we just proceed...
    if (chain.request().headers.names().contains("Authorization"))
      return chain.proceed(chain.request())

    val originalRequest = chain.request()

    lock.withLock {

      if (
        sessionManager.isAccessTokenExpired() ||
        sessionManager.getAccessToken().isNullOrEmpty() ||
        sessionManager.getAccessTokenAddress() != walletRepository.getDefaultWalletAddress()
        ) {
        renewToken()
      }
    }

    return makeRequest(chain, originalRequest)
  }

  private fun makeRequest(
    chain: Interceptor.Chain,
    originalRequest: Request,
    retry: Int = 1
  ): Response {
    val authorizedRequest = originalRequest.newBuilder()
      .apply { sessionManager.getAccessToken()?.let { header("Authorization", it) } }
      .build()

    val response = chain.proceed(authorizedRequest)

    if (response.code == UNAUTHORIZED && retry < MAX_RETRIES) {
      response.close()
      lock.withLock { renewToken() }
      return makeRequest(chain, originalRequest, retry + 1)
    }

    return response
  }

  private fun renewToken() {
    try {
      // it needs to be inside a try catch because we might not have a default wallet. for example on first run
      Log.d(this::class.java.simpleName, "Renewing token")
      val ewt = ewtAuthenticatorService.getEwtAuthentication().blockingGet()
      val activeWallet = walletRepository.getDefaultWalletAddress()
      val endDate = ewtAuthenticatorService.getSessionEndDate()

      if (ewt != null && endDate != null) {
        val response = renewJwtApi.renewJwt(ewt = ewt).blockingGet()
        val token = response.jwt

        Log.d(this::class.java.simpleName, token)
        sessionManager.updateAccessToken(token, activeWallet, endDate)
      }
    } catch (e: Throwable) {
      Log.e(this::class.java.simpleName, "Error renewing token")
      e.printStackTrace()
    }
  }
}
