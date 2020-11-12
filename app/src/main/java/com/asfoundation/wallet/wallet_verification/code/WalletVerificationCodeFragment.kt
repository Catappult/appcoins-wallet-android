package com.asfoundation.wallet.wallet_verification.code

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.wallet_verification.WalletVerificationActivityView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class WalletVerificationCodeFragment : DaggerFragment(), WalletVerificationCodeView {

  @Inject
  lateinit var presenter: WalletVerificationCodePresenter

  private lateinit var activityView: WalletVerificationActivityView

  companion object {

    @JvmStatic
    fun newInstance() = WalletVerificationCodeFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is WalletVerificationActivityView) {
      throw IllegalStateException(
          "Wallet Verification Code must be attached to Wallet Verification Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_verification_code, container, false)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

}