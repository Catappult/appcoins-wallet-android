package com.asfoundation.wallet.nfts.repository

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.nfts.domain.NFTItem
import io.reactivex.Single

class NftRepository(
  private val nftApi: NftApi,
  private val rxSchedulers: RxSchedulers
) {

  fun getNFTAssetList(address: String): Single<List<NFTItem>>{
    return nftApi.getNFTsOfWallet(address).map { response ->
      response.assets.map { assetResponse ->
        NFTItem(
          assetResponse.name,
          assetResponse.description,
          assetResponse.imagePreviewUrl,
          assetResponse.tokenId + assetResponse.contractAddress
        )
      }
    }.subscribeOn(rxSchedulers.io)
  }
}