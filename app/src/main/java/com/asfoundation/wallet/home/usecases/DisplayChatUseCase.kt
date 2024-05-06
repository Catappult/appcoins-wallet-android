package com.asfoundation.wallet.home.usecases

import com.wallet.appcoins.feature.support.data.SupportRepository
import javax.inject.Inject

class DisplayChatUseCase @Inject constructor(private val supportRepository: SupportRepository) {
  operator fun invoke(uid: String? = null) {
    supportRepository.openIntercom(uid = uid)
  }
}