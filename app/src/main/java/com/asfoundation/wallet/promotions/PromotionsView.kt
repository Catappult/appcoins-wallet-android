package com.asfoundation.wallet.promotions

import io.reactivex.Observable

interface PromotionsView {

  fun showNetworkErrorView()

  fun retryClick(): Observable<Any>

  fun showRetryAnimation()

  fun hideLoading()

  fun showLoading()

  fun showNoPromotionsScreen()

  fun showPromotions(promotionsModel: PromotionsModel)

  fun hidePromotions()

  fun getPromotionClicks(): Observable<PromotionClick>
}
