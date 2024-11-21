package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import android.util.Pair
import com.appcoins.wallet.core.network.base.IGetPrivateKeyUseCase
import com.appcoins.wallet.core.utils.jvm_common.WalletUtils
import com.appcoins.wallet.feature.walletInfo.data.AccountKeystoreService
import com.appcoins.wallet.feature.walletInfo.data.authentication.PasswordStore
import ethereumj.crypto.ECKey
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BoundTo(IGetPrivateKeyUseCase::class)
class GetPrivateKeyUseCase @Inject constructor(
  private val accountKeyService: AccountKeystoreService,
  private val passwordStore: PasswordStore,
) : IGetPrivateKeyUseCase {

  private var stringECKeyPair: Pair<String, ECKey>? = null

  override operator fun invoke(address: String): Single<ECKey> =
    if (stringECKeyPair?.first?.equals(address, true) == true) {
      Single.just(stringECKeyPair!!.second)
    } else {
      passwordStore.getPassword(address)
        .flatMap { password ->
          accountKeyService.exportAccount(address, password, password)
            .map { json ->
              ECKey.fromPrivate(WalletUtils.loadCredentials(password, json).ecKeyPair.privateKey)
            }
        }
        .doOnSuccess { ecKey -> stringECKeyPair = Pair(address, ecKey) }
    }
}
