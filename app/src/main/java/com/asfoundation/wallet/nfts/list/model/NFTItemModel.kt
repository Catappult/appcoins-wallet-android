package com.asfoundation.wallet.nfts.list.model

import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.NFTClick
import com.asfoundation.wallet.ui.common.BaseViewHolder


  @EpoxyModelClass
  abstract class NFTItemModel : EpoxyModelWithHolder<NFTItemModel.NFTItemHolder>() {

    @EpoxyAttribute
    lateinit var nftItem: NFTItem

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var clickListener: ((NFTClick) -> Unit)? = null

    override fun bind(holder: NFTItemHolder) {
      holder.title.text = nftItem.name
      holder.loadNft(nftItem.imageURL)
      holder.itemView.setOnClickListener{clickListener?.invoke(NFTClick(nftItem))}
    }

    override fun getDefaultLayout(): Int = R.layout.item_nft

    class NFTItemHolder : BaseViewHolder() {
      val title by bind<TextView>(R.id.nft_title)
      val image by bind<ImageView>(R.id.nft_image)
    }

    private fun NFTItemModel.NFTItemHolder.loadNft(url: String?) {
      GlideApp.with(itemView.context)
        .load(url)
        //.error(R.drawable.ic_promotions_default)
        .into(image)
    }

}