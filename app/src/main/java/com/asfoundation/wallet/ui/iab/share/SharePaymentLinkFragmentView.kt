package com.asfoundation.wallet.ui.iab.share

import io.reactivex.Observable

interface SharePaymentLinkFragmentView {

  fun getShareButtonClick(): Observable<SharePaymentData>

  fun getCancelButtonClick(): Observable<Any>

  fun shareLink(url: String)

  fun showFetchingLinkInfo()

  fun showErrorInfo()

  fun close()

  data class SharePaymentData(val domain: String, val skuId: String?,
                              val message: String?,
                              val originalAmount: String?,
                              val originalCurrency: String?)
}
