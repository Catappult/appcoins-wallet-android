package com.asfoundation.wallet.service

import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.util.WalletUtils
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil.sha3
import io.reactivex.Single
import org.web3j.crypto.Keys.toChecksumAddress


class AccountWalletService(private val walletInteract: FindDefaultWalletInteract,
                           private val accountKeyService: AccountKeystoreService,
                           private val passwordStore: PasswordStore,
                           private var normalizer: ContentNormalizer) : WalletService {

    private var stringECKeyPair: Pair<String, ethereumj.crypto.ECKey>? = null

    override fun getWalletAddress(): Single<String> {
        return walletInteract.find().map { wallet -> toChecksumAddress(wallet.address) }
    }

  override fun signContent(content: String): Single<String> {
    return walletInteract.find()
        .flatMap { wallet -> getPrivateKey(wallet).map { ecKey -> sign(normalizer.normalize(content), ecKey) } }
  }

    @Throws(Exception::class)
    fun sign(plainText: String, ecKey: ECKey): String {
        val signature = ecKey.sign(sha3(plainText.toByteArray()))
        return signature.toHex()
    }

  private fun getPrivateKey(wallet: Wallet): Single<ECKey> {
    if (stringECKeyPair != null && stringECKeyPair!!.first.equals(wallet.address, true)) {
      return Single.just(stringECKeyPair!!.second)
    }
    return passwordStore.getPassword(wallet)
        .flatMap { password ->
          accountKeyService.exportAccount(wallet, password, password)
              .map { json ->
                ECKey.fromPrivate(WalletUtils.loadCredentials(password, json)
                    .ecKeyPair
                    .privateKey)
              }
        }.doOnSuccess { ecKey -> stringECKeyPair = Pair(wallet.address, ecKey) }
  }

    interface ContentNormalizer {
        fun normalize(content: String): String
    }
}