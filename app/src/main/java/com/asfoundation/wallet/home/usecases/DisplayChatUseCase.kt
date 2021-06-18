package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.support.SupportRepository
import io.intercom.android.sdk.Intercom

class DisplayChatUseCase(private val supportRepository: SupportRepository) {

  operator fun invoke() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }
}