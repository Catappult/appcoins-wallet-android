package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.interact.ImportWalletInteract
import com.asfoundation.wallet.interact.WalletModel
import com.asfoundation.wallet.ui.iab.FiatValue
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Single

class ImportWalletPasswordInteractor(private val gson: Gson,
                                     private val balanceInteract: BalanceInteract,
                                     private val importWalletInteract: ImportWalletInteract) {

  fun extractWalletAddress(keystore: String): Single<String> {
    val parsedKeystore = gson.fromJson(keystore, Keystore::class.java)
    return Single.just("0x" + parsedKeystore.address)
  }

  fun getOverallBalance(address: String): Single<FiatValue> {
    return balanceInteract.getTotalBalance(address)
        .take(1)
        .single(FiatValue())
  }

  fun importWallet(keystore: String, password: String): Single<WalletModel> {
    return importWalletInteract.importKeystore(keystore, password)
  }

  fun setDefaultWallet(address: String): Completable {
    return importWalletInteract.setDefaultWallet(address)
  }

  private data class Keystore(val address: String)

}
