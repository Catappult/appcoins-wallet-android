package com.asfoundation.wallet.ui.webview_login.usecases

import android.os.Build
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetPrivateKeyUseCase
import io.reactivex.Single
import org.web3j.utils.Numeric
import javax.inject.Inject

class GenerateWebLoginUrlUseCase @Inject constructor(
  private val getPrivateKeyUseCase: GetPrivateKeyUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase
) {

  operator fun invoke(): Single<String> {
    return getCurrentWalletUseCase()
      .flatMap { key ->
        getPrivateKeyUseCase(key.address)
          .map { privateKey ->
            val hexKey = Numeric.toHexStringNoPrefixZeroPadded(privateKey.privKey, 64)
            val url =
              HostProperties.WEBVIEW_PAYMENT_URL +
                  "?domain=com.appcoins.wallet.dev&payment_channel=${mapPaymentChannel()}&user=$hexKey"
            url
          }
      }
  }

  fun isAnbox(): Boolean {
    return Build.PRODUCT.replace(";", " ").contains("anbox_arm64")
  }

  fun mapPaymentChannel(): String {
    return if (isAnbox()) {
      "wallet_app_cloud"
    } else {
      "wallet_app"
    }
  }

}
