package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import io.reactivex.Observable
import java.math.BigDecimal

/**
 * Created by franciscocalado on 19/07/2018.
 */
interface OnChainBuyView {
  val okErrorClick: Observable<Any?>
  val supportIconClick: Observable<Any?>
  val supportLogoClick: Observable<Any?>
  fun close(data: Bundle?)
  fun finish(data: Bundle?)
  fun showError()
  fun showTransactionCompleted()
  fun showWrongNetworkError()
  fun showNoNetworkError()
  fun showApproving()
  fun showBuying()
  fun showNonceError()
  fun showNoTokenFundsError()
  fun showNoEtherFundsError()
  fun showNoFundsError()
  fun showForbiddenError()
  fun showRaidenChannelValues(values: List<BigDecimal>)
  val animationDuration: Long
  fun lockRotation()
}