package com.asfoundation.wallet.nfts.domain

import java.io.Serializable

data class NFTItem(val name: String, val description: String?, val imageURL : String , val id : String) :
  Serializable


