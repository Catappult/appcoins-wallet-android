package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import io.reactivex.Single

interface FindDefaultWalletUseCase {
  operator fun invoke(): Single<Wallet>
}