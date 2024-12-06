package com.appcoins.wallet.core.network.base

import io.reactivex.Single
import ethereumj.crypto.ECKey

interface IGetPrivateKeyUseCase {

  operator fun invoke(address: String): Single<ECKey>
}
