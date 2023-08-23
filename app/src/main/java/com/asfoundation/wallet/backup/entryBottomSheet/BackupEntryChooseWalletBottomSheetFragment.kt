package com.asfoundation.wallet.backup.entryBottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.feature.backup.ui.R
import com.appcoins.wallet.feature.backup.ui.databinding.SettingsWalletBottomSheetLayoutBinding
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletsModel
import com.appcoins.wallet.ui.common.addBottomItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@AndroidEntryPoint
class BackupEntryChooseWalletBottomSheetFragment : BackupEntryChooseWalletBottomSheetView, BottomSheetDialogFragment(),
  Navigator {

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: BackupEntryChooseWalletBottomSheetPresenter

  private var uiEventListener: PublishSubject<String>? = null

  private val binding by viewBinding(SettingsWalletBottomSheetLayoutBinding::bind)

  companion object {

    const val WALLET_MODEL_KEY = "wallet_model"
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
    presenter.present(requireArguments().getSerializable(WALLET_MODEL_KEY) as WalletsModel, navController())
  }

  override fun setupUi(walletsBalance: List<WalletInfoSimple>) {
    with(binding.bottomSheetWalletsCards) {
      addBottomItemDecoration(resources.getDimension(R.dimen.wallets_card_margin))
      isNestedScrollingEnabled = false
      layoutManager = LinearLayoutManager(context).apply {
        orientation = RecyclerView.VERTICAL
      }
      adapter = BackupEntryChooseWalletAdapter(walletsBalance, uiEventListener!!, currencyFormatter)

    }
    provideParentFragment()?.showBottomSheet()
  }

  override fun walletCardClicked() = uiEventListener!!

  private fun provideParentFragment(): BackupEntryChooseWalletView? =
    if (parentFragment !is BackupEntryChooseWalletView) {
      null
    } else {
      parentFragment as BackupEntryChooseWalletView
    }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      com.asf.wallet.R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }
}
