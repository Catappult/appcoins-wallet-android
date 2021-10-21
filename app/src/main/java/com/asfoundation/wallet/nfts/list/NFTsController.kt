package com.asfoundation.wallet.nfts.list

import com.airbnb.epoxy.TypedEpoxyController
import com.asfoundation.wallet.nfts.domain.NFTItem
import com.asfoundation.wallet.nfts.list.model.NFTItemModel_

class NFTsController : TypedEpoxyController<List<NFTItem>>() {

  var clickListener: ((NFTClick) -> Unit)? = null

  override fun buildModels(data: List<NFTItem>) {
    for(nft in data){
      add(
        NFTItemModel_().id(nft.id).nftItem(nft).clickListener(clickListener)
      )
    }
  }
}