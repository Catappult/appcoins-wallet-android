package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Observable

interface VoucherDetailsView {

  fun setupUi(title: String, featureGraphic: String, icon: String, maxBonus: Double,
              packageName: String, hasAppcoins: Boolean)

  fun setupSkus(voucherSkuItems: List<VoucherSkuItem>)

  fun onNextClicks(): Observable<VoucherSkuItem>

  fun onCancelClicks(): Observable<Any>

  fun onBackPressed(): Observable<Any>

  fun onSkuButtonClick(): Observable<Int>

  fun onDownloadButtonClick(): Observable<Any>

  fun setSelectedSku(index: Int)

  fun onRetryClick(): Observable<Any>

  fun showRetryAnimation()

  fun showLoading()

  fun hideLoading()

  fun showNoNetworkError()
}