package com.asfoundation.wallet.ui.wallets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.databinding.RemoveWalletSecondLayoutBinding
import com.asfoundation.wallet.interact.DeleteWalletInteract
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@AndroidEntryPoint
class WalletRemoveConfirmationFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), WalletRemoveConfirmationView {

  @Inject
  lateinit var deleteWalletInteract: DeleteWalletInteract

  @Inject
  lateinit var logger: Logger
  private lateinit var presenter: WalletRemoveConfirmationPresenter
  private lateinit var activityView: RemoveWalletActivityView

  private val binding by viewBinding(RemoveWalletSecondLayoutBinding::bind)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = WalletRemoveConfirmationPresenter(this, walletAddress, deleteWalletInteract, logger,
        CompositeDisposable(), AndroidSchedulers.mainThread(), Schedulers.io())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is RemoveWalletActivityView) {
      throw IllegalStateException(
          "Wallet Confirmation must be attached to Remove Wallet Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = RemoveWalletSecondLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setWalletBalance()
    presenter.present()
  }

  override fun noButtonClick() = RxView.clicks(binding.noRemoveWalletButton)

  override fun yesButtonClick() = RxView.clicks(binding.yesRemoveWalletButton)

  override fun navigateBack() {
    activity?.onBackPressed()
  }

  override fun showRemoveWalletAnimation() = activityView.showRemoveWalletAnimation()

  override fun finish() = activityView.finish()

  override fun showAuthentication() = activityView.showAuthentication()

  override fun authenticationResult() = activityView.authenticationResult()

  private fun setWalletBalance() {
    binding.removeBalance.walletAddress.text = walletAddress
    binding.removeBalance.walletBalance.text = fiatBalance
    binding.removeBalance.balanceAppcoins.text = appcoinsBalance
    binding.removeBalance.balanceCredits.text = creditsBalance
    binding.removeBalance.balanceEthereum.text = ethereumBalance
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private val walletAddress: String by lazy {
    if (requireArguments().containsKey(WALLET_ADDRESS_KEY)) {
      requireArguments().getString(WALLET_ADDRESS_KEY)!!
    } else {
      throw IllegalArgumentException("walletAddress not found")
    }
  }

  private val fiatBalance: String by lazy {
    if (requireArguments().containsKey(FIAT_BALANCE_KEY)) {
      requireArguments().getString(FIAT_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("fiat balance not found")
    }
  }

  private val appcoinsBalance: String by lazy {
    if (requireArguments().containsKey(APPC_BALANCE_KEY)) {
      requireArguments().getString(APPC_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("appc balance not found")
    }
  }

  private val creditsBalance: String by lazy {
    if (requireArguments().containsKey(CREDITS_BALANCE_KEY)) {
      requireArguments().getString(CREDITS_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("credits balance not found")
    }
  }

  private val ethereumBalance: String by lazy {
    if (requireArguments().containsKey(ETHEREUM_BALANCE_KEY)) {
      requireArguments().getString(ETHEREUM_BALANCE_KEY)!!
    } else {
      throw IllegalArgumentException("ethereum balance not found")
    }
  }

  companion object {

    private const val WALLET_ADDRESS_KEY = "wallet_address"
    private const val FIAT_BALANCE_KEY = "fiat_balance"
    private const val APPC_BALANCE_KEY = "appc_balance"
    private const val CREDITS_BALANCE_KEY = "credits_balance"
    private const val ETHEREUM_BALANCE_KEY = "ethereum_balance"

    fun newInstance(walletAddress: String, totalFiatBalance: String,
                    appcoinsBalance: String, creditsBalance: String,
                    ethereumBalance: String): WalletRemoveConfirmationFragment {
      val fragment = WalletRemoveConfirmationFragment()
      Bundle().apply {
        putString(WALLET_ADDRESS_KEY, walletAddress)
        putString(FIAT_BALANCE_KEY, totalFiatBalance)
        putString(APPC_BALANCE_KEY, appcoinsBalance)
        putString(CREDITS_BALANCE_KEY, creditsBalance)
        putString(ETHEREUM_BALANCE_KEY, ethereumBalance)
        fragment.arguments = this
      }
      return fragment
    }
  }
}
