package com.appcoins.wallet.ui.widgets

import android.graphics.*
import android.os.Build
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CardHeaderTransformation(private val radius: Int) : BitmapTransformation() {
  var margin = 0
  fun transform(toTransform: Bitmap): Bitmap {
    val paint = Paint()
    paint.isAntiAlias = true
    paint.shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val output = Bitmap.createBitmap(
      toTransform.width, toTransform.height,
      Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(output)
    canvas.drawRect(
      RectF(
        margin.toFloat(), (margin + radius).toFloat(), (toTransform.width - margin).toFloat(),
        (
            toTransform.height - margin).toFloat()
      ), paint
    )
    canvas.drawRoundRect(
      RectF(
        margin.toFloat(), margin.toFloat(), (toTransform.width - margin).toFloat(),
        (
            toTransform.height - margin).toFloat()
      ), radius.toFloat(), radius.toFloat(), paint
    )
    if (toTransform != output) {
      toTransform.recycle()
    }
    return output
  }

  override fun transform(
    pool: BitmapPool, inBitmap: Bitmap, outWidth: Int,
    outHeight: Int
  ): Bitmap {
    val width = inBitmap.width
    val height = inBitmap.height
    val right = (width - margin).toFloat()
    val bottom = (height - margin).toFloat()

    // Alpha is required for this transformation.
    val safeConfig = getAlphaSafeConfig(inBitmap)
    val toTransform = getAlphaSafeBitmap(pool, inBitmap)
    val result = pool[toTransform.width, toTransform.height, safeConfig]
    result.setHasAlpha(true)
    val shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val paint = Paint()
    paint.isAntiAlias = true
    paint.shader = shader
    val canvas = Canvas(result)
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    canvas.drawRoundRect(
      RectF(margin.toFloat(), margin.toFloat(), right, (margin + radius * 2).toFloat()),
      radius.toFloat(),
      radius.toFloat(),
      paint
    )
    canvas.drawRect(RectF(margin.toFloat(), (margin + radius).toFloat(), right, bottom), paint)
    clear(canvas)
    if (toTransform != inBitmap) {
      pool.put(toTransform)
    }
    return result
  }

  override fun updateDiskCacheKey(messageDigest: MessageDigest) {
    messageDigest.update(ID_BYTES)
  }

  companion object {
    private val ID_BYTES = "card_header".toByteArray(CHARSET)
    private fun getAlphaSafeConfig(inBitmap: Bitmap): Bitmap.Config {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (Bitmap.Config.RGBA_F16 == inBitmap.config) {
          return Bitmap.Config.RGBA_F16
        }
      }
      return Bitmap.Config.ARGB_8888
    }

    private fun getAlphaSafeBitmap(
      pool: BitmapPool,
      maybeAlphaSafe: Bitmap
    ): Bitmap {
      val safeConfig = getAlphaSafeConfig(maybeAlphaSafe)
      if (safeConfig == maybeAlphaSafe.config) {
        return maybeAlphaSafe
      }
      val argbBitmap = pool[maybeAlphaSafe.width, maybeAlphaSafe.height, safeConfig]
      Canvas(argbBitmap).drawBitmap(maybeAlphaSafe, 0f, 0f, null)
      return argbBitmap
    }

    private fun clear(canvas: Canvas) {
      canvas.setBitmap(null)
    }
  }
}
