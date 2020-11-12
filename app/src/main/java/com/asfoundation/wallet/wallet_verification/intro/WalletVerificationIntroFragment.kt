package com.asfoundation.wallet.wallet_verification.intro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.wallet_verification.WalletVerificationActivityView
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class WalletVerificationIntroFragment : DaggerFragment(), WalletVerificationIntroView {

  @Inject
  lateinit var presenter: WalletVerificationIntroPresenter

  private lateinit var activityView: WalletVerificationActivityView

  companion object {

    @JvmStatic
    fun newInstance() = WalletVerificationIntroFragment()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is WalletVerificationActivityView) {
      throw IllegalStateException(
          "Wallet Verification Intro must be attached to Wallet Verification Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_verification_intro, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

}