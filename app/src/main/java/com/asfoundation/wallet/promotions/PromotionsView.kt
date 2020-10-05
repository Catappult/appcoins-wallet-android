package com.asfoundation.wallet.promotions

import io.reactivex.Observable

interface PromotionsView {

  fun showNetworkErrorView()

  fun hideNetworkErrorView()

  fun retryClick(): Observable<Any>

  fun showRetryAnimation()

  fun hideLoading()

  fun showLoading()

  fun showNoPromotionsScreen()

  fun showPromotions(promotionsModel: PromotionsModel)

  fun hidePromotions()

  fun getPromotionClicks(): Observable<PromotionClick>

  fun getHomeBackPressed(): Observable<Any>

  fun handleBackPressed()

  fun getBottomSheetButtonClick(): Observable<Any>

  fun getBackPressed(): Observable<Any>

  fun updateBottomSheetVisibility()

  fun showBottomSheet()
}
