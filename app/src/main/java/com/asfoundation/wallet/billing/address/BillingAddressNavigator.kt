package com.asfoundation.wallet.billing.address

import android.content.Intent
import com.asfoundation.wallet.ui.iab.IabActivity

class BillingAddressNavigator(private val fragment: BillingAddressFragment,
                              private val iabActivity: IabActivity) {

  fun finishWithSuccess(billingAddressModel: BillingAddressModel) {
    val intent = Intent().apply {
      putExtra(BillingAddressFragment.BILLING_ADDRESS_MODEL, billingAddressModel)
    }
    fragment.targetFragment?.onActivityResult(IabActivity.BILLING_ADDRESS_REQUEST_CODE,
        IabActivity.BILLING_ADDRESS_SUCCESS_CODE, intent)
    iabActivity.navigateBack()
  }

  fun finishWithCancel() {
    fragment.targetFragment?.onActivityResult(IabActivity.BILLING_ADDRESS_REQUEST_CODE,
        IabActivity.BILLING_ADDRESS_CANCEL_CODE, null)
    iabActivity.navigateBack()
  }
}
