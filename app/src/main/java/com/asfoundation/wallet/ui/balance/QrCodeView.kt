package com.asfoundation.wallet.ui.balance

import io.reactivex.Observable

interface QrCodeView {

  fun shareClick(): Observable<Any>
  fun copyClick(): Observable<Any>
  fun closeSuccess()
  fun closeClick(): Observable<Any>
  fun setAddressToClipBoard(walletAddress: String)
  fun showShare(walletAddress: String)
  fun setWalletAddress(walletAddress: String)
  fun createQrCode(walletAddress: String)
}
