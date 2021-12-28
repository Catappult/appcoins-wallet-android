package com.asfoundation.wallet.restore.password

import com.asfoundation.wallet.restore.intro.RestoreWalletInteractor
import com.asfoundation.wallet.wallets.WalletModel
import com.google.gson.Gson
import io.reactivex.Single

class RestoreWalletPasswordInteractor(private val gson: Gson,
                                      private val restoreWalletInteractor: RestoreWalletInteractor) {

  fun extractWalletAddress(keystore: String): Single<String> = Single.create {
    val parsedKeystore = gson.fromJson(keystore, Keystore::class.java)
    it.onSuccess("0x" + parsedKeystore.address)
  }

//  fun restoreWallet(keystore: String, password: String): Single<WalletModel> =
//      restoreWalletInteractor.restoreKeystore(keystore, password)

  fun setDefaultWallet(address: String) = restoreWalletInteractor.setDefaultWallet(address)

  private data class Keystore(val address: String)

}
