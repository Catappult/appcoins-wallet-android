package com.asfoundation.wallet.rating

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import io.reactivex.Completable
import java.util.Locale
import javax.inject.Inject

class RatingInteractor
@Inject
constructor(
    private val ratingRepository: RatingRepository,
    private val walletService: WalletService,
    private val rxSchedulers: RxSchedulers
) {

  fun sendUserFeedback(feedbackText: String): Completable {
    return walletService
        .getWalletAddress()
        .flatMap { address ->
          ratingRepository.sendFeedback(address.toLowerCase(Locale.ROOT), feedbackText)
        }
        .ignoreElement()
        .subscribeOn(rxSchedulers.io)
  }

  fun isNotFirstTime(): Boolean = ratingRepository.isNotFirstTime()

  fun setRemindMeLater() = ratingRepository.setRemindMeLater()

  fun setImpression() = ratingRepository.setImpression()
}
