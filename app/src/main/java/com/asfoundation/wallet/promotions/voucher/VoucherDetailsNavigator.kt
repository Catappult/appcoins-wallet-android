package com.asfoundation.wallet.promotions.voucher

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.appcoins.wallet.bdsbilling.repository.TransactionType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.util.toOneStepUri
import java.math.BigDecimal

class VoucherDetailsNavigator(private val fragmentManager: FragmentManager,
                              private val activity: FragmentActivity) {

  fun navigateToPurchaseFlow(voucherSkuItem: VoucherSkuItem, packageName: String) {
    val transactionBuilder = TransactionBuilder(voucherSkuItem.skuId, voucherSkuItem.title,
        BigDecimal(voucherSkuItem.price.value), voucherSkuItem.price.currency,
        voucherSkuItem.price.symbol, BigDecimal(voucherSkuItem.price.appc), packageName,
        TransactionType.VOUCHER.name, TransactionBuilder.APPCOINS_WALLET_ADDRESS)

    val intent = Intent().apply {
      data = transactionBuilder.toOneStepUri()
      putExtra(IabActivity.PRODUCT_NAME, transactionBuilder.skuId)
    }
    activity.startActivityForResult(
        IabActivity.newIntent(activity, intent, transactionBuilder, true,
            null), 111) // This request code will not be processed by PromotionsActivity
  }

  fun navigateBack() = fragmentManager.popBackStack()

  fun navigateToStore(packageName: String) {
    activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    )
  }

}
