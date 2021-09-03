package com.asfoundation.wallet

import android.content.Context
import android.os.Build
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions


@GlideModule
class AppGlideModule : AppGlideModule() {

  override fun applyOptions(context: Context, builder: GlideBuilder) {

    var requestOptions = RequestOptions()
    val decodeFormat: DecodeFormat
    if (Build.VERSION.SDK_INT >= 26) {
      decodeFormat = DecodeFormat.PREFER_ARGB_8888
      requestOptions = requestOptions.disallowHardwareConfig()
    } else {
      decodeFormat = DecodeFormat.PREFER_RGB_565
    }
    requestOptions = requestOptions.format(decodeFormat)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    builder.setDefaultRequestOptions(requestOptions)
  }

}