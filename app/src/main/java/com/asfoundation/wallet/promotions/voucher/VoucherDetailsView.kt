package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Observable

interface VoucherDetailsView {

  fun setupUi(title: String, featureGraphic: String, icon: String, maxBonus: Double,
              packageName: String, hasAppcoins: Boolean)

  fun setupSkus(skuButtonModels: List<SkuButtonModel>)

  fun onNextClicks(): Observable<SkuButtonModel>

  fun onCancelClicks(): Observable<Any>

  fun onBackPressed(): Observable<Any>

  fun onSkuButtonClick(): Observable<Int>

  fun onDownloadButtonClick(): Observable<Any>

  fun setSelectedSku(index: Int)
}