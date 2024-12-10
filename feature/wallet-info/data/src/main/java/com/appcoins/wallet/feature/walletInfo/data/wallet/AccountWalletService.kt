package com.appcoins.wallet.feature.walletInfo.data.wallet

import com.appcoins.wallet.core.network.base.ISignUseCase
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.walletservices.WalletServices.WalletAddressModel
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.WalletRepositoryType
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.CreateWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetPrivateKeyUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.RegisterFirebaseTokenUseCase
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.internal.schedulers.ExecutorScheduler
import it.czerwinski.android.hilt.annotations.BoundTo
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys.toChecksumAddress
import java.math.BigInteger
import javax.inject.Inject

@BoundTo(supertype = WalletService::class)
class AccountWalletService @Inject constructor(
  private val getPrivateKeyUseCase: GetPrivateKeyUseCase,
  private val signUseCase: ISignUseCase,
  private val passwordStore: PasswordStore,
  private val createWalletUseCase: CreateWalletUseCase,
  private val registerFirebaseTokenUseCase: RegisterFirebaseTokenUseCase,
  private val walletRepository: WalletRepositoryType,
  private val syncScheduler: ExecutorScheduler,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
) : WalletService {

  private val PRIVATE_RADIX = 16
  private val N_VALUE = 1 shl 9
  private val P_VALUE = 1

  override fun getWalletAddress(): Single<String> = find()
    .map { toChecksumAddress(it.address) }

  override fun getWalletOrCreate(): Single<String> = find()
    .subscribeOn(syncScheduler)
    .onErrorResumeNext { createWalletUseCase("Main Wallet") }
    .map { toChecksumAddress(it.address) }

  override fun findWalletOrCreate(): Observable<String> = find()
    .toObservable()
    .subscribeOn(syncScheduler)
    .map { wallet -> wallet.address }
    .onErrorResumeNext { _: Throwable ->
      Observable.just(WalletGetterStatus.CREATING.toString())
        .mergeWith(createWalletUseCase("Main Wallet").map { it.address }.toObservable())
        .flatMap {
          registerFirebaseTokenUseCase.registerFirebaseToken(wallet = Wallet(it))
            .map { wallet -> wallet.address }.toObservable()
        }
    }

  override fun signContent(content: String): Single<String> = find()
    .flatMap { wallet ->
      getPrivateKeyUseCase(wallet.address).map {
        signUseCase(content, it)
      }
    }

  override fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKeyUseCase(wallet.address)
        .map { signUseCase(toChecksumAddress(wallet.address), it) }
        .map { WalletAddressModel(wallet.address, it) }
    }

  override fun getAndSignSpecificWalletAddress(walletAddress: String): Single<WalletAddressModel> =
    walletRepository.findWallet(walletAddress)
      .flatMap { wallet ->
        getPrivateKeyUseCase(wallet.address)
          .map { signUseCase(toChecksumAddress(wallet.address), it) }
          .map { WalletAddressModel(wallet.address, it) }
      }

  private fun find(): Single<Wallet> = getCurrentWalletUseCase()

  interface ContentNormalizer {
    fun normalize(content: String): String
  }

  fun getAddressFromPrivateKey(key: String): Single<String> {
    val private = BigInteger(key, PRIVATE_RADIX)
    val keypair = ECKeyPair.create(private)
    return passwordStore.generatePassword()
      .map {
        val addressIncompl = org.web3j.crypto.Wallet.create(
          it,
          keypair,
          N_VALUE,
          P_VALUE
        ).address
        "0x$addressIncompl"
      }
  }

}

enum class WalletGetterStatus { CREATING }
