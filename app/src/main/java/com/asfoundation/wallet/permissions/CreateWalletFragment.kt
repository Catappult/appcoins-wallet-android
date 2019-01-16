package com.asfoundation.wallet.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import dagger.android.support.DaggerFragment

class CreateWalletFragment : DaggerFragment(), CreateWalletView {
  companion object {
    fun newInstance(): CreateWalletFragment {
      return CreateWalletFragment()
    }
  }

  private lateinit var presenter: CreateWalletPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = CreateWalletPresenter(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_create_wallet_layout, container, false)
  }
}
