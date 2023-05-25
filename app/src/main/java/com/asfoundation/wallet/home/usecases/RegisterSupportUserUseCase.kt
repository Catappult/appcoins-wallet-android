package com.asfoundation.wallet.home.usecases

import com.wallet.appcoins.feature.support.data.SupportRepository
import io.intercom.android.sdk.Intercom
import java.util.*
import javax.inject.Inject

class RegisterSupportUserUseCase @Inject constructor(
    private val supportRepository: SupportRepository) {

  operator fun invoke(level: Int, walletAddress: String) {
    // force lowercase to make sure 2 users are not registered with the same wallet address, where
    // one has uppercase letters (to be check summed), and the other does not
    val address = walletAddress.toLowerCase(Locale.ROOT)
    val currentUser = supportRepository.getCurrentUser()
    if (currentUser.userAddress != address || currentUser.gamificationLevel != level) {
      if (currentUser.userAddress != address) {
        Intercom.client()
            .logout()
      }
      supportRepository.saveNewUser(address, level)
    }
  }
}