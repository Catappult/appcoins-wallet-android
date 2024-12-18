package com.appcoins.wallet.core.network.base.repository

import com.appcoins.wallet.core.network.base.compat.RenewJwtApi
import javax.inject.Inject

class JwtRepository @Inject constructor(
  private val jwtApi: RenewJwtApi
) {

  fun getJwtFrom(ewt: String) =
    jwtApi.renewJwt(ewt)
}