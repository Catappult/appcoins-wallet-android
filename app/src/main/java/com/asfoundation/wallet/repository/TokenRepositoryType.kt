package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.Token
import io.reactivex.Single

interface TokenRepositoryType {
  fun getAppcBalance(address: String): Single<Token>
}
