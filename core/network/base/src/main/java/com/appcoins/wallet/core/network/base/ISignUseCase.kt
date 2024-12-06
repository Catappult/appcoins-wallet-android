package com.appcoins.wallet.core.network.base

import ethereumj.crypto.ECKey

interface ISignUseCase {
  operator fun invoke(plainText: String, ecKey: ECKey): String
}
