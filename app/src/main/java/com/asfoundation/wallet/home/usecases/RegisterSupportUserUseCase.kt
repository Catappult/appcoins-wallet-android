package com.asfoundation.wallet.home.usecases

import androidx.annotation.Keep
import com.wallet.appcoins.feature.support.data.SupportRepository
import javax.inject.Inject

@Keep
class RegisterSupportUserUseCase @Inject constructor(
  private val supportRepository: SupportRepository
) {

  operator fun invoke(level: Int, walletAddress: String) {
    supportRepository.registerUser(level, walletAddress)
  }
}