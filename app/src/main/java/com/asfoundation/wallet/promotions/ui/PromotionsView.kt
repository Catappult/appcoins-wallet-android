package com.asfoundation.wallet.promotions.ui

import com.asfoundation.wallet.promotions.model.PromotionsModel
import com.asfoundation.wallet.promotions.ui.list.PromotionClick
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

  fun handleBackPressed()

  fun getBottomSheetButtonClick(): Observable<Any>

  fun getBackPressed(): Observable<Any>

  fun hideBottomSheet()

  fun showBottomSheet()

  fun getBottomSheetContainerClick(): Observable<Any>

  fun showLockedPromotionsScreen()

  fun showToast()

  fun getVouchersRadioButtonClick(): Observable<Boolean>

  fun getPerksRadioButtonClick(): Observable<Boolean>

  fun checkVouchersRadioButton()

  fun checkPerksRadioButton()

  fun pageChangedCallback(): Observable<Int>

  fun changeButtonState(position: Int)
}
