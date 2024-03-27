package com.asfoundation.wallet.home.usecases

import com.wallet.appcoins.feature.support.data.SupportRepository
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.IntercomSpace
import javax.inject.Inject

class DisplayConversationListOrChatUseCase @Inject constructor(
  private val supportRepository: SupportRepository
) {

  operator fun invoke() {
    //this method was introduced because if the app is closed intercom returns 0 unread conversations
    //even if there are more
    supportRepository.resetUnreadConversations()
    val handledByIntercom = getUnreadConversations() > 0
    if (handledByIntercom) {
      Intercom.client()
        .present()
    } else {
      Intercom.client()
        .present(space = IntercomSpace.Messages)
    }
  }

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount
}