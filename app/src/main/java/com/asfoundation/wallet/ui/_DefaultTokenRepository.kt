package com.asfoundation.wallet.ui

import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.TokenInfo
import java.util.*

class _DefaultTokenRepository(defaultNetwork: NetworkInfo) {

  val tokenInfo: TokenInfo

  init {
    tokenInfo = when (defaultNetwork.chainId) {
      1 -> TokenInfo(
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS,
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_NAME,
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL.lowercase(Locale.getDefault()),
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS
      )
      3 -> TokenInfo(
        BuildConfig.ROPSTEN_DEFAULT_TOKEN_ADDRESS,
        BuildConfig.ROPSTEN_DEFAULT_TOKEN_NAME,
        BuildConfig.ROPSTEN_DEFAULT_TOKEN_SYMBOL.lowercase(Locale.getDefault()),
        BuildConfig.ROPSTEN_DEFAULT_TOKEN_DECIMALS
      )
      else -> TokenInfo(
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_ADDRESS,
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_NAME,
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_SYMBOL.lowercase(Locale.getDefault()),
        BuildConfig.MAIN_NETWORK_DEFAULT_TOKEN_DECIMALS
      )
    }
  }
}