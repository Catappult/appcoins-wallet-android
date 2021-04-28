package com.asfoundation.wallet.transfers

import io.reactivex.Observable

interface EtherTransactionBottomSheetView {

  fun setTransactionHash()

  fun getEtherScanClick(): Observable<Any>

  fun getClipboardClick(): Observable<Any>

  fun getOkClick(): Observable<Any>

  fun copyToClipboard()
}