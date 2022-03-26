package com.asfoundation.wallet.recover.use_cases

import com.google.gson.Gson
import io.reactivex.Single
import javax.inject.Inject

class ExtractWalletAddressUseCase @Inject constructor(private val gson: Gson) {

  operator fun invoke(keystore: String): Single<String> = Single.create {
    val parsedKeystore = gson.fromJson(keystore, Keystore::class.java)
    it.onSuccess("0x" + parsedKeystore.address)
  }

  private data class Keystore(val address: String)
}