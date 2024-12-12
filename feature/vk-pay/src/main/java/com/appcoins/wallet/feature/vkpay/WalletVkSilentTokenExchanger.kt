package com.appcoins.wallet.feature.vkpay

import com.vk.auth.main.SilentAuthSource
import com.vk.auth.main.VkFastLoginModifiedUser
import com.vk.auth.main.VkSilentTokenExchanger
import com.vk.auth.main.VkSilentTokenExchanger.Result
import com.vk.silentauth.SilentAuthInfo
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(VkSilentTokenExchanger::class)
class WalletVkSilentTokenExchanger @Inject constructor() : VkSilentTokenExchanger {
  override fun exchangeSilentToken(
    user: SilentAuthInfo,
    modifiedUser: VkFastLoginModifiedUser?,
    source: SilentAuthSource
  ): VkSilentTokenExchanger.Result {
    return Result.Error(NotImplementedError(), "silent tokens are not supported!")
    //return VkSilentTokenExchanger.Result.Success(user.token, user.getDistinctId())
  }
}