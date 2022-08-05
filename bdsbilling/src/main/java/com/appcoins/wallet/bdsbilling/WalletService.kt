package com.appcoins.wallet.bdsbilling

import io.reactivex.Observable
import io.reactivex.Single

interface WalletService {

  fun getWalletAddress(): Single<String>

  fun getWalletOrCreate(): Single<String>

  fun findWalletOrCreate(): Observable<String>

  fun signContent(content: String): Single<String>

  fun signSpecificWalletAddressContent(walletAddress: String, content: String): Single<String>

  fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel>

  fun getAndSignSpecificWalletAddress(walletAddress :String): Single<WalletAddressModel>
}