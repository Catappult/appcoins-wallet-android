package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.RestoreWalletInteractor
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.Single

class RestoreWalletPasswordInteractor(private val gson: Gson,
                                      private val balanceInteract: BalanceInteract,
                                      private val restoreWalletInteractor: RestoreWalletInteractor) {

  fun extractWalletAddress(keystore: String): Single<String> = Single.create {
    val parsedKeystore = gson.fromJson(keystore, Keystore::class.java)
    it.onSuccess("0x" + parsedKeystore.address)
  }

  fun getOverallBalance(address: String): Observable<FiatValue> =
      balanceInteract.getTotalBalance(address)

  fun restoreWallet(keystore: String, password: String): Single<WalletModel> =
      restoreWalletInteractor.restoreKeystore(keystore, password)

  fun setDefaultWallet(address: String) = restoreWalletInteractor.setDefaultWallet(address)

  private data class Keystore(val address: String)

}
