package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.core.network.base.ISignUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.repository.SignDataStandardNormalizer
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil.sha3
import javax.inject.Inject

class SignUseCase @Inject constructor() : ISignUseCase {

  private val normalizer by lazy { SignDataStandardNormalizer() }

  override operator fun invoke(plainText: String, ecKey: ECKey): String =
    ecKey.sign(sha3(normalizer.normalize(plainText).toByteArray())).toHex()
}
