package com.asfoundation.wallet.nfts.list.model

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.NFTClick
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.facebook.shimmer.ShimmerFrameLayout


@EpoxyModelClass
abstract class NFTItemModel : EpoxyModelWithHolder<NFTItemModel.NFTItemHolder>() {

  @EpoxyAttribute
  lateinit var nftItem: NFTItem

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((NFTClick) -> Unit)? = null

  override fun bind(holder: NFTItemHolder) {
    holder.title.text = nftItem.name
    holder.description.text = nftItem.description
    holder.loadNft(nftItem.imageURL)
    holder.image.transitionName = nftItem.id
    holder.itemView.setOnClickListener { clickListener?.invoke(NFTClick(nftItem)) }
  }

  override fun getDefaultLayout(): Int = R.layout.item_nft

  class NFTItemHolder : BaseViewHolder() {
    val description by bind<TextView>(R.id.nft_subtitle)
    val title by bind<TextView>(R.id.nft_title)
    val image by bind<ImageView>(R.id.nft_image)
    val skeletonImage by bind<ShimmerFrameLayout>(R.id.nft_image_skeleton)
    val skeletonText by bind<ShimmerFrameLayout>(R.id.nft_title_skeleton)
  }

  private fun NFTItemHolder.loadNft(url: String?) {
    skeletonImage.visibility = View.VISIBLE
    skeletonText.visibility = View.VISIBLE
    GlideApp.with(itemView.context)
        .asBitmap()
        .load(url)
        .transition(BitmapTransitionOptions.withCrossFade())
        .listener(SkeletonGlideRequestListener(skeletonImage, skeletonText))
        .error(R.drawable.nfts_error)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(image)
  }

  internal class SkeletonGlideRequestListener(private val skeletonImage: View,
                                              private val skeletonText: View) :
      RequestListener<Bitmap> {

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?,
                              isFirstResource: Boolean): Boolean {
      skeletonImage.visibility = View.GONE
      skeletonText.visibility = View.GONE
      return false
    }

    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?,
                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
      skeletonImage.visibility = View.GONE
      skeletonText.visibility = View.GONE
      return false
    }
  }

}