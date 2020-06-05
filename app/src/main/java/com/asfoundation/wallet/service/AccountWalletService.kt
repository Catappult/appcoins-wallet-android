package com.asfoundation.wallet.service

import android.util.Log
import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.interact.WalletCreatorInteract
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.util.WalletUtils
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil.sha3
import io.reactivex.Single
import io.reactivex.internal.schedulers.ExecutorScheduler
import org.web3j.crypto.Keys.toChecksumAddress


class AccountWalletService(private val walletInteract: FindDefaultWalletInteract,
                           private val accountKeyService: AccountKeystoreService,
                           private val passwordStore: PasswordStore,
                           private val walletCreatorInteract: WalletCreatorInteract,
                           private val normalizer: ContentNormalizer,
                           private val syncScheduler: ExecutorScheduler,
                           private val walletRepository: WalletRepositoryType) : WalletService {

  private var stringECKeyPair: Pair<String, ECKey>? = null

  override fun getWalletAddress(): Single<String> {
    return walletInteract.find()
        .map { wallet -> toChecksumAddress(wallet.address) }
  }

  override fun getWalletOrCreate(): Single<String> {
    return walletInteract.find().subscribeOn(syncScheduler).onErrorResumeNext {
      Log.e("TEST","**** FAILED to get wallet, creating one, AccountWalletService")
      walletCreatorInteract.create()
    }
        .map { wallet -> toChecksumAddress(wallet.address) }
  }

  override fun signContent(content: String): Single<String> {
    return walletInteract.find()
        .flatMap { wallet ->
          getPrivateKey(wallet).map { ecKey ->
            sign(normalizer.normalize(content), ecKey)
          }
        }
  }

  override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> {
    return walletInteract.find()
        .flatMap { wallet ->
          getPrivateKey(wallet).map { ecKey ->
            sign(normalizer.normalize(toChecksumAddress(wallet.address)), ecKey)
          }
              .map { WalletAddressModel(wallet.address, it) }
        }
  }

  @Throws(Exception::class)
  fun sign(plainText: String, ecKey: ECKey): String {
    val signature = ecKey.sign(sha3(plainText.toByteArray()))
    return signature.toHex()
  }

  fun find(): Single<Wallet> {
    return walletRepository.defaultWallet
        .onErrorResumeNext {
          Log.e("TEST","**** FAILEd to get default wallet, FindDefaultWalletInteract")
          walletRepository.fetchWallets()
              .filter { wallets: Array<Wallet?> -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                walletRepository.setDefaultWallet(wallet.address)
              }
              .andThen(
                  walletRepository.defaultWallet)
        }
  }

  private fun getPrivateKey(wallet: Wallet): Single<ECKey> {
    if (stringECKeyPair != null && stringECKeyPair!!.first.equals(wallet.address, true)) {
      return Single.just(stringECKeyPair!!.second)
    }
    return passwordStore.getPassword(wallet.address)
        .flatMap { password ->
          accountKeyService.exportAccount(wallet.address, password, password)
              .map { json ->
                ECKey.fromPrivate(WalletUtils.loadCredentials(password, json)
                    .ecKeyPair
                    .privateKey)
              }
        }
        .doOnSuccess { ecKey -> stringECKeyPair = Pair(wallet.address, ecKey) }
  }

  interface ContentNormalizer {
    fun normalize(content: String): String
  }
}