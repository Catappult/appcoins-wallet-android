package com.asfoundation.wallet.ui.webview_login.usecases

import android.content.Context
import android.os.Build
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.Wallet
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCurrentWalletUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.GetEncryptedPrivateKeyUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import javax.inject.Inject

class GenerateWebLoginUrlUseCase @Inject constructor(
  private val getEncryptedPrivateKeyUseCase: GetEncryptedPrivateKeyUseCase,
  private val getCurrentWalletUseCase: GetCurrentWalletUseCase,
  @ApplicationContext private val context: Context,
) {

  operator fun invoke(): Single<String> {
    return Single.zip(
      getCurrentWalletUseCase(), getEncryptedPrivateKeyUseCase()
    ) { currentWallet: Wallet, encryptedKey: String ->
      val url =
        HostProperties.WEBVIEW_LOGIN_URL +
            "?domain=${context.packageName}&payment_channel=${mapPaymentChannel()}&address=${currentWallet.address}&user=$encryptedKey"
      return@zip url
    }
  }

  fun isAnbox(): Boolean {
    return Build.PRODUCT.replace(";", " ").contains("anbox_arm64")
  }

  fun mapPaymentChannel(): String {
//    return "wallet_app_cloud"  //to test web login
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
