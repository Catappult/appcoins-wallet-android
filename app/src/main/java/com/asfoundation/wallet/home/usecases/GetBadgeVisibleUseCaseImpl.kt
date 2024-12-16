package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.ui.widgets.top_bar.use_case.GetBadgeVisibleUseCase
import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UnreadConversationCountListener
import io.reactivex.Observable
import it.czerwinski.android.hilt.annotations.BoundTo
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BoundTo(GetBadgeVisibleUseCase::class)
class GetBadgeVisibleUseCaseImpl @Inject constructor() : GetBadgeVisibleUseCase {

  override operator fun invoke() = Observable.create<Boolean> { emitter ->
    emitter.onNext(Intercom.client().unreadConversationCount > 0)
    val unreadListener =
      UnreadConversationCountListener { unreadCount -> emitter.onNext(unreadCount > 0) }

    with(Intercom.client()) {
      addUnreadConversationCountListener(unreadListener)

      emitter.setCancellable {
        removeUnreadConversationCountListener(unreadListener)
      }
    }
  }.asFlow()
}