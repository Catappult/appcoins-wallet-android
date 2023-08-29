package com.asfoundation.wallet.home.usecases

import com.wallet.appcoins.feature.support.data.SupportRepository
import io.intercom.android.sdk.Intercom
import javax.inject.Inject

class DisplayChatUseCase @Inject constructor(private val supportRepository: SupportRepository) {

  operator fun invoke() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }
}