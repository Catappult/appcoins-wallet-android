package com.asfoundation.wallet.promotions.voucher

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.bdsbilling.repository.TransactionType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.IabActivity
import java.math.BigDecimal

class VoucherDetailsNavigator(private val fragmentManager: FragmentManager,
                              private val activity: FragmentActivity) {

  fun navigateToPurchaseFlow(voucherSkuItem: VoucherSkuItem, packageName: String) {
    val transactionBuilder = TransactionBuilder(voucherSkuItem.skuId, voucherSkuItem.title,
        BigDecimal(voucherSkuItem.price.value), voucherSkuItem.price.currency,
        voucherSkuItem.price.symbol, BigDecimal(voucherSkuItem.price.appc), packageName,
        TransactionType.VOUCHER.name, TransactionBuilder.APPCOINS_WALLET_ADDRESS)
    activity.startActivity(IabActivity.newIntent(activity, null, transactionBuilder, true, null))
  }

  fun navigateBack() = fragmentManager.popBackStack()

  fun navigateToStore(packageName: String) {
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    )
  }

}
