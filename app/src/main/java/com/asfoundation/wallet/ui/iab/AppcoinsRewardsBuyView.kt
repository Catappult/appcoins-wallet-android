package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import io.reactivex.Observable

internal interface AppcoinsRewardsBuyView {
  fun finish(purchase: Purchase?)
  fun showLoading()
  fun hideLoading()
  fun showNoNetworkError()
  val okErrorClick: Observable<Any>
  val supportIconClick: Observable<Any>
  val supportLogoClick: Observable<Any>
  fun close()
  fun showGenericError()
  fun showError(message: Int?)
  fun finish(uid: String)
  fun errorClose()
  fun finish(purchase: Purchase, orderReference: String)
  fun showTransactionCompleted()
  val animationDuration: Long
  fun lockRotation()
}