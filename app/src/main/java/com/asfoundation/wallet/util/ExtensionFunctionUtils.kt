package com.asfoundation.wallet.util

import android.content.res.Resources
import android.graphics.*
import android.view.WindowManager
import com.asf.wallet.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 *
 * Class file to create kotlin extension functions
 *
 */

fun BigDecimal.scaleToString(scale: Int): String {
  val format = DecimalFormat("#.##")
  return format.format(this.setScale(scale, RoundingMode.FLOOR))
}

fun BigDecimal.formatWithSuffix(scale: Int): String {
  val suffixFormatter = NumberFormatterUtils.create()
  val scaledNumber = this.setScale(scale, RoundingMode.FLOOR)
  return suffixFormatter.formatNumberWithSuffix(scaledNumber.toFloat(), scale)
}

fun Throwable?.isNoNetworkException(): Boolean {
  return this != null && (this is IOException || this.cause != null && this.cause is IOException)
}

fun Bitmap.mergeWith(centeredImage: Bitmap): Bitmap {

  val combined = Bitmap.createBitmap(width, height, config)
  val canvas = Canvas(combined)
  canvas.drawBitmap(this, Matrix(), null)

  val resizeLogo =
      Bitmap.createScaledBitmap(centeredImage, canvas.width / 5, canvas.height / 5, true)
  val centreX = (canvas.width - resizeLogo.width) / 2f
  val centreY = (canvas.height - resizeLogo.height) / 2f
  canvas.drawBitmap(resizeLogo, centreX, centreY, null)
  return combined
}

fun String.generateQrCode(resources: Resources, windowManager: WindowManager): Bitmap {
  val size = Point()
  windowManager.defaultDisplay
      .getSize(size)
  val imageSize = (size.x * 0.9).toInt()
  val bitMatrix =
      MultiFormatWriter().encode(this, BarcodeFormat.QR_CODE, imageSize, imageSize,
          null)
  val barcodeEncoder = BarcodeEncoder()
  val bitmapLogo = BitmapFactory.decodeResource(resources, R.drawable.ic_appc_token)
  val qrCode = barcodeEncoder.createBitmap(bitMatrix)
  return qrCode.mergeWith(bitmapLogo)
}