package com.appcoins.wallet.billing

import io.reactivex.Single


interface WalletService {

    fun getWalletAddress(): Single<String>

    fun signContent(content: String): Single<String>
}