package com.asfoundation.wallet.change_currency.svg

//import android.graphics.Picture
//import android.graphics.drawable.PictureDrawable
//import com.bumptech.glide.load.Options
//import com.bumptech.glide.load.engine.Resource
//import com.bumptech.glide.load.resource.SimpleResource
//import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
//import com.caverock.androidsvg.SVG
//
//
///**
// * Convert the [SVG]'s internal representation to an Android-compatible one ([Picture]).
// */
//class SvgDrawableTranscoder :
//    ResourceTranscoder<SVG?, PictureDrawable> {
//  override fun transcode(
//      toTranscode: Resource<SVG?>, options: Options): Resource<PictureDrawable>? {
//    val svg: SVG = toTranscode.get()
//    val picture: Picture = svg.renderToPicture()
//    val drawable = PictureDrawable(picture)
//    return SimpleResource(drawable)
//  }
//}