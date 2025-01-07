package com.wallet.appcoins.feature.support.data

import io.reactivex.Completable
import javax.inject.Inject

class SupportInteractor @Inject constructor(
  private val supportRepository: SupportRepository,
) {

  fun showSupport(uid: String? = null): Completable {
    return openIntercom(uid)
  }

  private fun openIntercom(uid: String?): Completable {
    return Completable.fromAction {
      supportRepository.openIntercom(uid)
    }
  }

  fun hasNewUnreadConversations() = supportRepository.hasUnreadConversations()
}