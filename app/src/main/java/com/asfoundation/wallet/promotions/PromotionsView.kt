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

  fun getPromotionClicks(): Observable<PromotionClick>

  fun getHomeBackPressed(): Observable<Any>

  fun handleBackPressed()

  fun getBottomSheetButtonClick(): Observable<Any>

  fun getBackPressed(): Observable<Any>

  fun hideBottomSheet()

  fun showBottomSheet()

  fun getBottomSheetContainerClick(): Observable<Any>

  fun showLockedPromotionsScreen()
}
