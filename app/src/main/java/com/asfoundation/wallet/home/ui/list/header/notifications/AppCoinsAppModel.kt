package com.asfoundation.wallet.home.ui.list.header.notifications

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.widget.holder.ApplicationClickAction
import com.appcoins.wallet.ui.widgets.CardHeaderTransformation
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.button.MaterialButton

@EpoxyModelClass
abstract class AppCoinsAppModel : EpoxyModelWithHolder<AppCoinsAppModel.AppHolder>() {

  @EpoxyAttribute
  var appCoinsApplication: AppcoinsApplication? = null

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((AppcoinsApplication, ApplicationClickAction) -> Unit)? = null

  override fun getDefaultLayout(): Int = R.layout.item_appcoins_application

  override fun bind(holder: AppHolder) {
    appCoinsApplication?.let { app ->
      holder.appName.text = app.name

      val marketBitmap: Target<Bitmap> = object : Target<Bitmap> {
        override fun onLoadStarted(placeholder: Drawable?) {
          holder.appIcon.setImageDrawable(placeholder)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
          val whiteBackground = ColorDrawable(0xffff)
          holder.appIcon.setImageDrawable(whiteBackground)
          holder.featuredGraphic.setImageDrawable(whiteBackground)
        }

        override fun onResourceReady(resource: Bitmap,
                                     transition: Transition<in Bitmap>?) {
          holder.appIcon.setImageBitmap(resource)
          if (app.featuredGraphic == null) {
            loadDefaultFeaturedGraphic(holder, resource)
          }
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
        override fun getSize(cb: SizeReadyCallback) {
          cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        }

        override fun removeCallback(cb: SizeReadyCallback) = Unit
        override fun onStart() = Unit
        override fun onStop() = Unit
        override fun onDestroy() = Unit
        override fun setRequest(request: Request?) = Unit
        override fun getRequest(): Request? = null
      }
      holder.appIcon.tag = marketBitmap

      GlideApp.with(holder.itemView.context)
          .asBitmap()
          .load(app.icon)
          .apply(RequestOptions.bitmapTransform(CircleCrop())
              .placeholder(android.R.drawable.progress_indeterminate_horizontal))
          .into(marketBitmap)

      val space = getSizeFromDp(holder.itemView.context.resources.displayMetrics, 8)
      GlideApp.with(holder.itemView.context)
          .load(app.featuredGraphic)
          .apply(RequestOptions.bitmapTransform(
              MultiTransformation(CenterCrop(), CardHeaderTransformation(space))))
          .into(holder.featuredGraphic)
      holder.appRating.text = app.rating.toString()
      setupClickListeners(holder, app)
    }
  }

  private fun setupClickListeners(holder: AppHolder, app: AppcoinsApplication) {
    holder.appName.setOnClickListener {
      clickListener?.invoke(app, ApplicationClickAction.CLICK)
    }
    holder.appIcon.setOnClickListener {
      clickListener?.invoke(app, ApplicationClickAction.CLICK)
    }
    holder.appRating.setOnClickListener {
      clickListener?.invoke(app, ApplicationClickAction.CLICK)
    }
    holder.featuredGraphic.setOnClickListener {
      clickListener?.invoke(app, ApplicationClickAction.CLICK)
    }
    holder.shareButton.setOnClickListener {
      clickListener?.invoke(app, ApplicationClickAction.SHARE)
    }
  }

  private fun loadDefaultFeaturedGraphic(holder: AppHolder, bitmap: Bitmap) {
    Palette.from(bitmap)
        .generate { palette: Palette? ->
          val dominantColor = palette!!.getDominantColor(0x36aeeb)
          val displayMetrics: DisplayMetrics = holder.itemView.context.resources.displayMetrics
          val space: Int = getSizeFromDp(displayMetrics, 8)
          val image =
              Bitmap.createBitmap(getSizeFromDp(displayMetrics, 260),
                  getSizeFromDp(displayMetrics, 16), Bitmap.Config.ARGB_8888)
          image.eraseColor(dominantColor)
          holder.featuredGraphic.setImageBitmap(
              addGradient(CardHeaderTransformation(space).transform(image),
                  palette.getDominantColor(0x36aeeb), palette.getDominantColor(0x36aeeb) + 300))
        }
  }

  private fun getSizeFromDp(displayMetrics: DisplayMetrics, value: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), displayMetrics)
        .toInt()
  }

  private fun addGradient(src: Bitmap, color1: Int, color2: Int): Bitmap? {
    val w = src.width
    val h = src.height
    val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)
    canvas.drawBitmap(src, 0f, 0f, null)
    val paint = Paint()
    val shader = LinearGradient(0f, 0f, w.toFloat(), 0f, color1, color2, Shader.TileMode.CLAMP)
    paint.shader = shader
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    return result
  }

  class AppHolder : BaseViewHolder() {
    val appName by bind<TextView>(R.id.app_name)
    val appIcon by bind<ImageView>(R.id.app_icon)
    val featuredGraphic by bind<ImageView>(R.id.featured_graphic)
    val appRating by bind<TextView>(R.id.app_rating)
    val shareButton by bind<MaterialButton>(R.id.share_button)
  }
}