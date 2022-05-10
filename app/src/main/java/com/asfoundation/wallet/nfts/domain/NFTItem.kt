package com.asfoundation.wallet.nfts.domain

import java.io.Serializable
import java.math.BigDecimal

data class NFTItem(val name: String?, val description: String?, val imageURL: String?,
                   val id: String, val schema: String, val tokenId: BigDecimal,
                   val contractAddress: String) : Serializable