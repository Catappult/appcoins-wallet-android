package com.asfoundation.wallet.ui.webview_login.usecases

import android.os.Build
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetEncryptedPrivateKeyUseCase
import io.reactivex.Single
import javax.inject.Inject

class GenerateWebLoginUrlUseCase @Inject constructor(
  private val getEncryptedPrivateKeyUseCase: GetEncryptedPrivateKeyUseCase,
) {

  operator fun invoke(): Single<String> {
    return getEncryptedPrivateKeyUseCase()
      .map { encyptedKey ->
        val url =
          HostProperties.WEBVIEW_LOGIN_URL +
              "?domain=com.appcoins.wallet.dev&payment_channel=${mapPaymentChannel()}&user=$encyptedKey"
        url
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

  fun isCloudGaming(): Boolean {
    return mapPaymentChannel() == "wallet_app_cloud"
  }

}
