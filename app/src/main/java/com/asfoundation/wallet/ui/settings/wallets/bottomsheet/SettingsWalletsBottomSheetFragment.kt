package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.wallets.SettingsWalletsView
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.ui.wallets.WalletsAdapter
import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.addBottomItemDecoration
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.settings_wallet_bottom_sheet_layout.*
import javax.inject.Inject

class SettingsWalletsBottomSheetFragment : BasePageViewFragment(), SettingsWalletsBottomSheetView {

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SettingsWalletsBottomSheetPresenter

  private lateinit var adapter: WalletsAdapter
  private var uiEventListener: PublishSubject<String>? = null

  companion object {

    const val WALLET_MODEL_KEY = "wallet_model"

    @JvmStatic
    fun newInstance(walletsModel: WalletsModel): SettingsWalletsBottomSheetFragment {
      return SettingsWalletsBottomSheetFragment().apply {
        arguments = Bundle().apply {
          putSerializable(WALLET_MODEL_KEY, walletsModel)
        }
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.settings_wallet_bottom_sheet_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun setupUi(walletsBalance: List<WalletBalance>) {
    val layoutManager = LinearLayoutManager(context)
    layoutManager.orientation = RecyclerView.VERTICAL
    adapter = WalletsAdapter(requireContext(), walletsBalance, uiEventListener!!, currencyFormatter)
    bottom_sheet_wallets_cards.addBottomItemDecoration(
        resources.getDimension(R.dimen.wallets_card_margin))
    bottom_sheet_wallets_cards.isNestedScrollingEnabled = false
    bottom_sheet_wallets_cards.layoutManager = layoutManager
    bottom_sheet_wallets_cards.adapter = adapter
    val parent = provideParentFragment()
    parent?.showBottomSheet()
  }

  override fun walletCardClicked() = uiEventListener!!

  private fun provideParentFragment(): SettingsWalletsView? {
    if (parentFragment !is SettingsWalletsView) {
      return null
    }
    return parentFragment as SettingsWalletsView
  }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}
