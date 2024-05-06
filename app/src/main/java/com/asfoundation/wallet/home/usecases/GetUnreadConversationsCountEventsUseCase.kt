package com.asfoundation.wallet.home.usecases

import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UnreadConversationCountListener
import io.reactivex.Observable
import javax.inject.Inject

class GetUnreadConversationsCountEventsUseCase @Inject constructor() {

  operator fun invoke() = Observable.create<Int> { emitter ->
    emitter.onNext(Intercom.client().unreadConversationCount)
    val unreadListener =
      UnreadConversationCountListener { unreadCount -> emitter.onNext(unreadCount) }

    with(Intercom.client()) {
      addUnreadConversationCountListener(unreadListener)

      emitter.setCancellable {
        removeUnreadConversationCountListener(unreadListener)
      }
    }
  }
}