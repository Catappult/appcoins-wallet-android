package com.asfoundation.wallet.util

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.WindowManager
import com.appcoins.wallet.ui.common.mergeWith
import com.appcoins.wallet.ui.common.toBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

fun String.generateQrCode(windowManager: WindowManager, logo: Drawable): Bitmap {
  val size = Point()
  windowManager.defaultDisplay
    .getSize(size)
  val imageSize = (size.x * 0.9).toInt()
  val bitMatrix =
    MultiFormatWriter().encode(
      this, BarcodeFormat.QR_CODE, imageSize, imageSize,
      null
    )
  val barcodeEncoder = BarcodeEncoder()
  val qrCode = barcodeEncoder.createBitmap(bitMatrix)
  return qrCode.mergeWith(logo.toBitmap())
}