package com.appcoins.wallet.feature.vkpay

import android.app.Activity
import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import com.vk.auth.main.VkClientUiInfo
import com.vk.dto.common.id.UserId
import com.vk.superapp.SuperappKit
import com.vk.superapp.SuperappKitConfig
import com.vk.superapp.core.SuperappConfig
import com.vk.superapp.vkpay.checkout.VkPayCheckout
import com.vk.superapp.vkpay.checkout.api.dto.model.VkMerchantInfo
import com.vk.superapp.vkpay.checkout.api.dto.model.VkTransactionInfo
import com.vk.superapp.vkpay.checkout.config.VkPayCheckoutConfig
import com.vk.superapp.vkpay.checkout.config.VkPayCheckoutConfigBuilder
import com.vk.superapp.vkpay.checkout.data.VkCheckoutUserInfo

object VkPayManager {

  fun initSuperAppKit(
    appName: String,
    clientSecret: String,
    context: Context,
    iconResources: Int,
    vkSdkAppId: String,
    activity: Activity?
  ) {
    val icon = AppCompatResources.getDrawable(context, iconResources)!!
    val appInfo = SuperappConfig.AppInfo(
      appName,
      vkSdkAppId,
      "1.232"
    )

    val config = activity?.let {
      SuperappKitConfig.Builder(it.application)
        .setAuthModelData(clientSecret)
        .setAuthUiManagerData(VkClientUiInfo(icon, appName))
        .setLegalInfoLinks(
          serviceUserAgreement = "https://id.vk.com/terms",
          servicePrivacyPolicy = "https://id.vk.com/privacy"
        )
        .setApplicationInfo(appInfo)
        .setUseCodeFlow(true)
        .build()
    }

    if (!SuperappKit.isInitialized()) {
      config?.let { SuperappKit.init(it) }
    }
  }

  fun checkoutVkPay(
    hash: String,
    uidTransaction: String,
    walletAddress: String,
    amount: Int,
    vkMerchantId: Int,
    vkSdkAppId: Int,
    fragmentManager: FragmentManager
  ) {
    val transaction = VkTransactionInfo(
      amount,
      uidTransaction, VkTransactionInfo.Currency.RUB
    )
    val merchantInfo = VkMerchantInfo(
      vkMerchantId,
      hash, walletAddress, "wallet APPC"
    )

    //This Val need to implement only in Developer Mode
    val config = if (BuildConfig.DEBUG) {
      val sandbox = VkPayCheckoutConfig.Environment.Sandbox(
        userInfo = VkCheckoutUserInfo(UserId(12345), "+1234566790"),
        useApi = false,
        mockNotCreatedVkPay = true,
        useTestMerchant = true,
        domain = VkPayCheckoutConfig.Domain.TEST
      )
      VkPayCheckoutConfigBuilder(merchantInfo).setParentAppId(vkSdkAppId)
        .setEnvironment(sandbox).build()
    } else {
      VkPayCheckoutConfigBuilder(merchantInfo).setParentAppId(vkSdkAppId)
        .build()
    }
    VkPayCheckout.startCheckout(fragmentManager, transaction, config)

  }
}