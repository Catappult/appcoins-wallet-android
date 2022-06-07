package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.interact.EmptyNotification
import com.asfoundation.wallet.update_required.use_cases.GetUnwatchedUpdateNotificationUseCase
import com.asfoundation.wallet.promotions.PromotionsInteractor
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetCardNotificationsUseCase @Inject constructor(
  private val referralInteractor: ReferralInteractorContract,
  private val getUnwatchedUpdateNotificationUseCase: GetUnwatchedUpdateNotificationUseCase,
  private val promotionsInteractor: PromotionsInteractor
) {

  operator fun invoke(): Single<List<CardNotification>> {
    val getUnwatchedPendingBonusNotification =
      referralInteractor.getUnwatchedPendingBonusNotification()
        .subscribeOn(Schedulers.io())
    val getUnwatchedUpdateNotification = getUnwatchedUpdateNotificationUseCase()
      .subscribeOn(Schedulers.io())
    val getUnwatchedPromotionNotification = promotionsInteractor.getUnwatchedPromotionNotification()
      .subscribeOn(Schedulers.io())
    return Single.zip(
      getUnwatchedPendingBonusNotification,
      getUnwatchedUpdateNotification,
      getUnwatchedPromotionNotification
    ) { referralNotification: CardNotification, updateNotification: CardNotification, promotionNotification: CardNotification ->
      val list = ArrayList<CardNotification>()
      if (referralNotification !is EmptyNotification) list.add(referralNotification)
      if (updateNotification !is EmptyNotification) list.add(updateNotification)
      if (promotionNotification !is EmptyNotification) list.add(promotionNotification)
      list
    }
  }
}