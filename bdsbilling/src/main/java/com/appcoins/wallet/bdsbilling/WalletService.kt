package com.appcoins.wallet.bdsbilling

import io.reactivex.Single


interface WalletService {

    fun getWalletAddress(): Single<String>

    fun signContent(content: String): Single<String>
}