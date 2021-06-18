package com.asfoundation.wallet.service

import android.util.Pair
import com.appcoins.wallet.bdsbilling.WalletAddressModel
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.util.WalletUtils
import com.asfoundation.wallet.wallets.WalletCreatorInteract
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil.sha3
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.schedulers.ExecutorScheduler
import org.web3j.crypto.Keys.toChecksumAddress


class AccountWalletService(private val accountKeyService: AccountKeystoreService,
                           private val passwordStore: PasswordStore,
                           private val walletCreatorInteract: WalletCreatorInteract,
                           private val normalizer: ContentNormalizer,
                           private val walletRepository: WalletRepositoryType,
                           private val syncScheduler: ExecutorScheduler) : WalletService {

  private var stringECKeyPair: Pair<String, ECKey>? = null

  override fun getWalletAddress(): Single<String> {
    return find()
        .map { wallet -> toChecksumAddress(wallet.address) }
  }

  override fun getWalletOrCreate(): Single<String> {
    return find()
        .subscribeOn(syncScheduler)
        .onErrorResumeNext {
          walletCreatorInteract.create()
        }
        .map { wallet -> toChecksumAddress(wallet.address) }
  }

  override fun findWalletOrCreate(): Observable<String> {
    return find()
        .toObservable()
        .subscribeOn(syncScheduler)
        .map { wallet -> wallet.address }
        .onErrorResumeNext { _: Throwable ->
          Observable.just(WalletGetterStatus.CREATING.toString())
              .mergeWith(
                  walletCreatorInteract.create()
                      .toObservable()
                      .map { wallet -> wallet.address })
        }
  }

  override fun signContent(content: String): Single<String> {
    return find()
        .flatMap { wallet ->
          getPrivateKey(wallet).map { ecKey ->
            sign(normalizer.normalize(content), ecKey)
          }
        }
  }

  override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> {
    return find()
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
    return walletRepository.getDefaultWallet()
        .onErrorResumeNext {
          walletRepository.fetchWallets()
              .filter { wallets -> wallets.isNotEmpty() }
              .map { wallets: Array<Wallet> ->
                wallets[0]
              }
              .flatMapCompletable { wallet: Wallet ->
                walletRepository.setDefaultWallet(wallet.address)
              }
              .andThen(walletRepository.getDefaultWallet())
        }
  }

  fun create(): Single<Wallet> = walletCreatorInteract.create()

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

enum class WalletGetterStatus { CREATING }
