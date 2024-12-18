package com.appcoins.wallet.core.network.base

import com.appcoins.wallet.core.network.base.repository.JwtRepository
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtAuthenticatorService @Inject constructor(
  private val ewtAuthenticatorService: EwtAuthenticatorService,
  private val jwtRepository: JwtRepository
) {

  fun getJwtAuthenticationWithAddress(address: String): Single<String> {
    return Single.just(ewtAuthenticatorService.getEwtAuthentication(address))
      .flatMap { jwtRepository.getJwtFrom(it) }
      .map { it.jwt }
  }
}