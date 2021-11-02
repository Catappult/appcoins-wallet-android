package com.asfoundation.wallet.nfts.list

import androidx.navigation.fragment.FragmentNavigator
import com.asfoundation.wallet.nfts.domain.NFTItem

data class NFTClick (
    val data: NFTItem,
    val extras: FragmentNavigator.Extras
)