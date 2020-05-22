package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.RestoreWalletInteractor
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single

class RestoreWalletPasswordInteractor(private val gson: Gson,
                                      private val balanceInteract: BalanceInteract,
                                      private val restoreWalletInteractor: RestoreWalletInteractor) {

  fun extractWalletAddress(keystore: String): Single<String> {
    val parsedKeystore = gson.fromJson(keystore, Keystore::class.java)
    return Single.just("0x" + parsedKeystore.address)
  }

  fun getOverallBalance(address: String): Single<FiatValue> {
    return balanceInteract.getTotalBalance(address)
        .firstOrError()
  }

  fun restoreWallet(keystore: String, password: String): Single<WalletModel> {
    return restoreWalletInteractor.restoreKeystore(keystore, password)
  }

  fun setDefaultWallet(address: String): Completable {
    return restoreWalletInteractor.setDefaultWallet(address)
  }

  private data class Keystore(val address: String)

}
