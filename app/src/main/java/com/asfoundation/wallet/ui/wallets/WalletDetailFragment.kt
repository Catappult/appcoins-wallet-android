package com.asfoundation.wallet.ui.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.wallet_detail_layout.*

class WalletDetailFragment : DaggerFragment(), WalletDetailView {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.wallet_detail_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    address.text = walletAddress
    if (isActive) is_active.text = "true" else is_active.text = "false"
  }

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val IS_ACTIVE_KEY = "is_active"

    fun newInstance(walletAddress: String, isActive: Boolean): WalletDetailFragment {
      val bundle = Bundle()
      val fragment = WalletDetailFragment()
      bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
      bundle.putBoolean(IS_ACTIVE_KEY, isActive)
      fragment.arguments = bundle
      return fragment
    }
  }

  private val walletAddress: String by lazy {
    if (arguments!!.containsKey(WALLET_ADDRESS_KEY)) {
      arguments!!.getString(WALLET_ADDRESS_KEY)
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val isActive: Boolean by lazy {
    if (arguments!!.containsKey(IS_ACTIVE_KEY)) {
      arguments!!.getBoolean(IS_ACTIVE_KEY)
    } else {
      throw IllegalArgumentException("is active not found")
    }
  }
}
