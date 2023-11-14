package com.wallet.appcoins.feature.support.data

import io.intercom.android.sdk.Intercom
import javax.inject.Inject

class DisplayChatUseCase @Inject constructor(private val supportRepository: SupportRepository) {

  operator fun invoke() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }
}