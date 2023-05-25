package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.ui.common.addBottomItemDecoration
import com.asf.wallet.R
import com.asf.wallet.databinding.SettingsWalletBottomSheetLayoutBinding
import com.asfoundation.wallet.ui.settings.wallets.SettingsWalletsView
import com.asfoundation.wallet.ui.wallets.WalletsAdapter
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class SettingsWalletsBottomSheetFragment : BasePageViewFragment(), SettingsWalletsBottomSheetView {

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SettingsWalletsBottomSheetPresenter

  private var uiEventListener: PublishSubject<String>? = null

  private val binding by viewBinding(SettingsWalletBottomSheetLayoutBinding::bind)

  companion object {

    const val WALLET_MODEL_KEY = "wallet_model"

    @JvmStatic
    fun newInstance(walletsModel: WalletsModel) =
      SettingsWalletsBottomSheetFragment().apply {
        arguments = Bundle().apply {
          putSerializable(WALLET_MODEL_KEY, walletsModel)
        }
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = SettingsWalletBottomSheetLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUi(walletInfoSimple: List<WalletInfoSimple>) {
    with(binding.bottomSheetWalletsCards) {
      addBottomItemDecoration(resources.getDimension(R.dimen.wallets_card_margin))
      isNestedScrollingEnabled = false
      layoutManager = LinearLayoutManager(context).apply {
        orientation = RecyclerView.VERTICAL
      }
      adapter = WalletsAdapter(walletInfoSimple, uiEventListener!!, currencyFormatter)
    }
    provideParentFragment()?.showBottomSheet()
  }

  override fun walletCardClicked() = uiEventListener!!

  private fun provideParentFragment(): SettingsWalletsView? =
    if (parentFragment !is SettingsWalletsView) {
      null
    } else {
      parentFragment as SettingsWalletsView
    }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}
