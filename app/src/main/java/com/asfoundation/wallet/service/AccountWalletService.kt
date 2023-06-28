package com.asfoundation.wallet.service

import android.util.Pair
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.utils.jvm_common.WalletUtils
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PasswordStore
import com.asfoundation.wallet.repository.SignDataStandardNormalizer
import com.asfoundation.wallet.repository.WalletRepositoryType
import com.asfoundation.wallet.wallets.WalletCreatorInteract
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil.sha3
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.schedulers.ExecutorScheduler
import it.czerwinski.android.hilt.annotations.BoundTo
import org.web3j.crypto.Keys.toChecksumAddress
import javax.inject.Inject

@BoundTo(supertype = WalletService::class)
class AccountWalletService @Inject constructor(
  private val accountKeyService: AccountKeystoreService,
  private val passwordStore: PasswordStore,
  private val walletCreatorInteract: WalletCreatorInteract,
  private val walletRepository: WalletRepositoryType,
  private val syncScheduler: ExecutorScheduler
) : WalletService {

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: Pair<String, ECKey>? = null

  override fun getWalletAddress(): Single<String> = find()
    .map { toChecksumAddress(it.address) }

  override fun getWalletOrCreate(): Single<String> = find()
    .subscribeOn(syncScheduler)
    .onErrorResumeNext { walletCreatorInteract.create("Main Wallet") }
    .map { toChecksumAddress(it.address) }

  override fun findWalletOrCreate(): Observable<String> = find()
    .toObservable()
    .subscribeOn(syncScheduler)
    .map { wallet -> wallet.address }
    .onErrorResumeNext { _: Throwable ->
      Observable.just(WalletGetterStatus.CREATING.toString())
        .mergeWith(walletCreatorInteract.create("Main Wallet").map { it.address }.toObservable())
    }

  override fun signContent(content: String): Single<String> = find()
    .flatMap { wallet -> getPrivateKey(wallet).map { sign(normalizer.normalize(content), it) } }

  override fun signSpecificWalletAddressContent(
    walletAddress: String,
    content: String
  ): Single<String> = walletRepository.findWallet(walletAddress)
    .flatMap { wallet -> getPrivateKey(wallet).map { sign(normalizer.normalize(content), it) } }

  override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKey(wallet)
        .map { sign(normalizer.normalize(toChecksumAddress(wallet.address)), it) }
        .map { WalletAddressModel(wallet.address, it) }
    }

  override fun getAndSignSpecificWalletAddress(walletAddress: String): Single<WalletAddressModel> =
    walletRepository.findWallet(walletAddress)
      .flatMap { wallet ->
        getPrivateKey(wallet)
          .map { sign(normalizer.normalize(toChecksumAddress(wallet.address)), it) }
          .map { WalletAddressModel(wallet.address, it) }
      }

  @Throws(Exception::class)
  private fun sign(plainText: String, ecKey: ECKey): String =
    ecKey.sign(sha3(plainText.toByteArray())).toHex()

  private fun find(): Single<Wallet> = walletRepository.getDefaultWallet()
    .onErrorResumeNext {
      walletRepository.fetchWallets()
        .filter { it.isNotEmpty() }
        .map { it[0] }
        .flatMapCompletable { walletRepository.setDefaultWallet(it.address) }
        .andThen(walletRepository.getDefaultWallet())
    }

  private fun getPrivateKey(wallet: Wallet): Single<ECKey> =
    if (stringECKeyPair?.first?.equals(wallet.address, true) == true) {
      Single.just(stringECKeyPair!!.second)
    } else {
      passwordStore.getPassword(wallet.address)
        .flatMap { password ->
          accountKeyService.exportAccount(wallet.address, password, password)
            .map { json ->
              ECKey.fromPrivate(WalletUtils.loadCredentials(password, json).ecKeyPair.privateKey)
            }
        }
        .doOnSuccess { ecKey -> stringECKeyPair = Pair(wallet.address, ecKey) }
    }

  interface ContentNormalizer {
    fun normalize(content: String): String
  }
}

enum class WalletGetterStatus { CREATING }
