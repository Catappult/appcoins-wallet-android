package com.asfoundation.wallet.topup.vkPayment

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appcoins.wallet.core.arch.data.Navigator
import javax.inject.Inject

class VkPaymentTopUpNavigator @Inject constructor(
    private val fragment: Fragment,
    private val packageManager: PackageManager
) :
    Navigator {
    fun navigateBack() {
        fragment.findNavController().popBackStack()
    }
}