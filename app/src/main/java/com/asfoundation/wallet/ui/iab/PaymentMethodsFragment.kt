package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PaymentMethodsFragment : DaggerFragment() {
  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.payment_methods_layout, container, false)
  }

  companion object {
    @JvmStatic
    fun newInstance(): PaymentMethodsFragment {
      val args = Bundle()
      val fragment = PaymentMethodsFragment()
      fragment.arguments = args
      return fragment
    }
  }
}
