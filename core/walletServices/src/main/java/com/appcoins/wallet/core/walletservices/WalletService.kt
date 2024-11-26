package com.appcoins.wallet.core.walletservices

import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import io.reactivex.Observable
import io.reactivex.Single

interface WalletService {

  fun getWalletAddress(): Single<String>

  fun getWalletOrCreate(): Single<String>

  fun findWalletOrCreate(): Observable<String>

  fun signContent(content: String): Single<String>

  fun getAndSignWalletAddress(walletAddress: String): Single<WalletAddressModel>

  fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel>

  fun getAndSignSpecificWalletAddress(walletAddress: String): Single<WalletAddressModel>
}
