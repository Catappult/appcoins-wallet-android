package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import io.reactivex.Completable

interface SetActiveWalletUseCase {
  operator fun invoke(wallet: Wallet): Completable
  operator fun invoke(walletAddress: String): Completable
}