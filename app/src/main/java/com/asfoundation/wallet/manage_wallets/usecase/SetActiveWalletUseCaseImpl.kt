package com.asfoundation.wallet.manage_wallets.usecase

import com.appcoins.wallet.feature.walletInfo.data.WalletRepository
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.SetActiveWalletUseCase
import com.asfoundation.wallet.gamification.ObserveUserStatsUseCase
import com.wallet.appcoins.feature.support.data.SupportRepository
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BoundTo(SetActiveWalletUseCase::class)
class SetActiveWalletUseCaseImpl @Inject constructor(
  private val observeUserStatsUseCase: ObserveUserStatsUseCase,
  private val walletRepository: WalletRepository,
  private val supportRepository: SupportRepository
) : SetActiveWalletUseCase {

  override operator fun invoke(wallet: Wallet): Completable =
    this(wallet.address)

  override operator fun invoke(walletAddress: String): Completable =
    walletRepository.setDefaultWallet(walletAddress)
      .andThen(refreshIntercomUserLogin())

  private fun refreshIntercomUserLogin(): Completable =
    Single.zip(
      walletRepository.getDefaultWallet(),
      observeUserStatsUseCase(false).firstOrError()
    ) { wallet, userStats -> wallet to userStats }
      .flatMapCompletable { (wallet, userStats) ->
        supportRepository.changeUser(
          walletAddress = wallet.address,
          level = userStats.level
        )
      }
}