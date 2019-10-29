package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.Token
import com.asfoundation.wallet.entity.Wallet
import io.reactivex.Single

interface TokenRepositoryType {
  fun getAppcBalance(wallet: Wallet): Single<Token>
}
