package com.appcoins.wallet.bdsbilling

import io.reactivex.Single


interface WalletService {

  fun getWalletAddress(): Single<String>

  @Deprecated("Use signContent() instead to avoid duplicated code")
  fun signContent(content: String): Single<String>

  fun signContent(): Single<WalletAddressModel>
}