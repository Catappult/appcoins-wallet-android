package com.asfoundation.wallet.promotions.voucher

import io.reactivex.Observable

interface EVoucherDetailsView {

  fun setupUi(title: String, packageName: String, skuButtonModels: List<SkuButtonModel>)

  fun onNextClicks(): Observable<SkuButtonModel>

  fun onCancelClicks(): Observable<Any>

  fun onBackPressed(): Observable<Any>

  fun onSkuButtonClick(): Observable<Int>

  fun onDownloadAppButtonClick(): Observable<Any>

  fun setSelectedSku(index: Int)
}